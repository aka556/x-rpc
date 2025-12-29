package org.xiaoyu.core.client.serviceCenter.balance.Impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.core.client.serviceCenter.balance.LoadBalance;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaoyu
 * @description 最近最久未使用负载均衡算法
 */
@Slf4j
public class LRULoadBalance implements LoadBalance {
    // 用于存储可用的服务器地址，按访问顺序排列
    // 使用ArrayList来实现LRU逻辑
    private final List<String> addressList = new ArrayList<>();
    
    // 维护每个地址的最后访问时间戳
    private final Map<String, Long> accessTimeMap = new HashMap<>();
    
    // 读写锁，保证线程安全
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }

        lock.writeLock().lock();
        try {
            // 更新地址列表，移除不存在的地址，添加新地址
            updateAddressList(addressList);
            
            if (this.addressList.isEmpty()) {
                throw new IllegalArgumentException("No servers available after update");
            }
            
            // 找到最久未访问的节点（最小时间戳的节点）
            String selectedAddress = findLeastRecentlyUsedAddress();
            
            // 更新访问时间
            accessTimeMap.put(selectedAddress, System.currentTimeMillis());
            
            log.info("LRU负载均衡选择了服务器：{}", selectedAddress);
            return selectedAddress;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 更新地址列表，保持与传入列表一致
     */
    private void updateAddressList(List<String> newAddressList) {
        // 移除不再存在的地址
        Iterator<String> iterator = this.addressList.iterator();
        while (iterator.hasNext()) {
            String address = iterator.next();
            if (!newAddressList.contains(address)) {
                iterator.remove();
                accessTimeMap.remove(address);
            }
        }
        
        // 添加新地址
        for (String address : newAddressList) {
            if (!this.addressList.contains(address)) {
                this.addressList.add(address);
                // 设置初始访问时间
                if (!accessTimeMap.containsKey(address)) {
                    accessTimeMap.put(address, System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * 查找最久未使用的地址
     */
    private String findLeastRecentlyUsedAddress() {
        String lruAddress = null;
        long earliestTime = Long.MAX_VALUE;
        
        for (String address : this.addressList) {
            Long accessTime = accessTimeMap.get(address);
            if (accessTime != null && accessTime < earliestTime) {
                earliestTime = accessTime;
                lruAddress = address;
            }
        }
        
        return lruAddress;
    }

    @Override
    public void addNode(String node) {
        lock.writeLock().lock();
        try {
            if (!addressList.contains(node)) {
                addressList.add(node);
                accessTimeMap.put(node, System.currentTimeMillis());
                log.info("LRU负载均衡添加了服务器{}", node);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeNode(String node) {
        lock.writeLock().lock();
        try {
            addressList.remove(node);
            accessTimeMap.remove(node);
            log.info("LRU负载均衡移除了服务器{}", node);
        } finally {
            lock.writeLock().unlock();
        }
    }
}