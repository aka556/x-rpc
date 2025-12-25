package org.xiaoyu.core.client.serviceCenter;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.xiaoyu.core.client.cache.ServiceCache;
import org.xiaoyu.core.client.serviceCenter.ZKWatcher.Watcher;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.ConsistencyHashLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

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
    public InetSocketAddress serviceDiscovery(String serviceName) {
        try {
            // 先从本地缓存中查找服务
            List<String> serviceList = cache.getServiceFromCache(serviceName);
            if (serviceList == null) {
                serviceList = client.getChildren().forPath("/" + serviceName);
            }
            String address = new ConsistencyHashLoadBalance().balance(serviceList);
            return parseAddress(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canResty = false;
        try {
            List<String> serviceList = client.getChildren().forPath("/" + RETRY);
            for(String s : serviceList) {
                if(s.equals(serviceName)) {
                    System.out.println("服务" + serviceName + "在白名单中，可以重试");
                    canResty = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return canResty;
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
