package org.xiaoyu.core.client.serviceCenter;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.core.client.cache.ServiceCache;
import org.xiaoyu.core.client.serviceCenter.ZKWatcher.Watcher;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.ConsistencyHashLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class ZKServerCenter implements ServiceCenter {
    // curator提供的zookeeper客户端
    private CuratorFramework client;
    // zookeeper根路径节点
    private static final String ROOT_PATH = "fufu";
    // 重试策略
    private static final String RETRY = "CanRetry";

    // cache
    private ServiceCache cache;

    // 负责zookeeper客户端的初始化,并与zookeeper客户端建立连接
    public ZKServerCenter() throws InterruptedException {
        // 指数时间重试
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // zookeeper的地址固定，不管是服务提供者还是，消费者都要与之建立连接
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(40000).retryPolicy(retryPolicy).namespace(ROOT_PATH).build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
        // 初始化本地缓存
        cache = new ServiceCache();
        // 监听器
        Watcher watcher = new Watcher(client, cache);
        // 启动监听
        watcher.watchToUpdate(ROOT_PATH);
    }

    @Override
    public InetSocketAddress serviceDiscovery(RpcRequest request) {
        String serviceName = request.getInterfaceName();
        try {
            // 先从本地缓存中查找服务
            List<String> serviceList = cache.getServiceFromCache(serviceName);
            if (serviceList == null) {
                serviceList = client.getChildren().forPath("/" + serviceName);
                List<String> cachedAddress = cache.getServiceFromCache(serviceName);
                if (cachedAddress == null || cachedAddress.isEmpty()) {
                    for (String address : serviceList) {
                        cache.addServiceToCache(serviceName, address);
                    }
                }
            }

            if (serviceList.isEmpty()) {
                log.error("没有可用的服务提供者，服务名：{}", serviceName);
                return null;
            }
            // 负载均衡得到地址
            String address = new ConsistencyHashLoadBalance().balance(serviceList);
            return parseAddress(address);
        } catch (Exception e) {
            log.error("服务发现失败，服务名：{}，错误信息：{}", serviceName, e.getMessage(), e);
        }
        return null;
    }

    // 使用线程安全的集合
    private Set<String> retryServiceCache = new CopyOnWriteArraySet<String>();

    // 使用白名单缓存，节省开销
    @Override
    public boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature) {
        if (retryServiceCache.isEmpty()) {
            try {
                CuratorFramework rootClient = client.usingNamespace(RETRY);
                List<String> retryableMethods = rootClient.getChildren().forPath("/" + getServiceAddress(serviceAddress));
                retryServiceCache.addAll(retryableMethods);
            } catch (Exception e) {
                log.error("检查重试失败， 方法签名: {}", methodSignature, e);
            }

        }
        return retryServiceCache.contains(methodSignature);
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * 地址获取辅助函数
     */
    private String getServiceAddress(InetSocketAddress serverAddress) {
        return serverAddress.getHostName() + ":" + serverAddress.getPort();
    }

    // 解析地址字符串
    private InetSocketAddress parseAddress(String address) {
        String[] result = address.split(":");
        return new InetSocketAddress(result[0], Integer.parseInt(result[1]));
    }
}
