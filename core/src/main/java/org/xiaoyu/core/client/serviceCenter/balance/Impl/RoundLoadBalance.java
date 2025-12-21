package org.xiaoyu.core.client.serviceCenter.balance.Impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.core.client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询法
 */
@Slf4j
public class RoundLoadBalance implements LoadBalance {
    // 线程安全
    private AtomicInteger choose = new AtomicInteger(0); // 用于表示当前服务节点的索引
    private final List<String> addressList = new CopyOnWriteArrayList<>();

    @Override
    public String balance(List<String> addressList) {
       if (addressList == null || addressList.isEmpty()) {
           throw new IllegalArgumentException("Address list cannot be null or empty");
       }

        int current = choose.getAndUpdate(i -> (i + 1) % addressList.size()); // 获取当前索引
        return addressList.get(current);
    }

    @Override
    public void addNode(String node) {
        // 动态添加节点
        addressList.add(node);
        log.info("轮询负载均衡添加了服务器{}", node);
    }

    @Override
    public void removeNode(String node) {
        addressList.remove(node);
        log.info("轮询负载均衡移除了服务器{}", node);
    }
}
