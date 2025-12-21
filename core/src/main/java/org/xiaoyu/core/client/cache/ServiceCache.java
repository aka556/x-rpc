package org.xiaoyu.core.client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ServiceCache {
    // 成员变量，存储服务名和地址列表
    private static Map<String, List<String>> cache = new HashMap<>();

    // 添加服务
    public void addServiceToCache(String serviceName, String address) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.add(address);
            System.out.println("将name为" + serviceName + "和地址为" + address + "的服务添加到本地缓存中");
        } else {
            List<String> addressList = new ArrayList<>();
            addressList.add(address);
            cache.put(serviceName, addressList);
        }
    }

    // 修改服务
    public void replaceServiceToCache(String serviceName, String oldAddress, String newAddress) {
        if (cache.containsKey(serviceName)) {
            List<String> addressList = cache.get(serviceName);
            addressList.remove(oldAddress);
            addressList.add(newAddress);
        } else {
            System.out.println("修改失败，服务不存在");
        }
    }

    // 从缓存中获取服务地址列表
    public List<String> getServiceFromCache(String serviceName) {
        if (!cache.containsKey(serviceName)) {
            log.warn("服务{}未找到", serviceName);
            // 返回不可修改的空列表,避免调用的时候出现空指针异常
            return Collections.emptyList();
        }
        return cache.get(serviceName);
    }

    // 从缓存中删除服务地址
    public void deleteServiceFromCache(String serviceName, String address) {
        List<String> addressList = cache.get(serviceName);
        if (addressList != null && addressList.contains(address)) {
            addressList.remove(address);
            log.info("将name为{} 和地址为{}的服务从本地缓存中删除了", serviceName, address);
            if (addressList.isEmpty()) {
                cache.remove(serviceName); // 移除该服务的缓存条目
                log.info("name为{}的服务已无地址，已从本地缓存中删除", serviceName);
            }
        } else {
            log.warn("删除失败， 地址{}不在服务{}的缓存列表中", address, serviceName);
        }
    }

}
