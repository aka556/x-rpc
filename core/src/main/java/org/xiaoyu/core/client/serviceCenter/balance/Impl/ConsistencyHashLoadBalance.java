package org.xiaoyu.core.client.serviceCenter.balance.Impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.core.client.serviceCenter.balance.LoadBalance;

import java.util.*;

/**
 * 一致性哈希负载均衡
 */
@Slf4j
public class ConsistencyHashLoadBalance implements LoadBalance {
    // 定义虚拟节点个数
    private static final int VIRTUAL_NUM = 5;

    // 保存虚拟节点的hash值和对应的虚拟节点,key为hash值，value为虚拟节点的名称
    @Getter
    public SortedMap<Integer, String> shards = new TreeMap<Integer, String>();
    // 真实节点列表
    @Getter // 可以被外部访问
    public List<String> realNodes = new LinkedList<String>();
    // 模拟初始服务器
    private String[] servers = null;

    // 获取虚拟节点
    public static int getVirtualNum() {
        return VIRTUAL_NUM;
    }

    //该方法初始化负载均衡器，将真实的服务节点和对应的虚拟节点添加到哈希环
    public void init(List<String> serviceList) {
        for (String server : serviceList) {
            realNodes.add(server);
            log.info("真实节点[{}] 被添加", server);
            // 遍历 serviceList（真实节点列表），每个真实节点都会生成 VIRTUAL_NUM
            // 个虚拟节点，并计算它们的哈希值
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = server + "&&VN" + i;
                int hash = getHash(virtualNode);
                // shards 是一个 SortedMap，会根据哈希值对虚拟节点进行排序
                shards.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        }
    }

    // 根据请求的 node（比如某个请求的标识符），选择一个服务器节点
    public String getServer(String node, List<String> serviceList) {
        // 初始化哈希环
        init(serviceList);
        
        // 如果服务列表为空，抛出异常
        if (shards.isEmpty()) {
            throw new IllegalArgumentException("No servers available");
        }
        
        // 计算哈希值
        int hash = getHash(node);
        Integer key = null;
        // 使用 shards.tailMap(hash) 获取 hash 值大于等于请求哈希值的所有虚拟节点
        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        // 如果没有找到，意味着请求的哈希值大于所有虚拟节点的哈希值，选择哈希值最大的虚拟节点。
        // 否则，选择 tailMap 中第一个虚拟节点
        if (subMap.isEmpty()) {
            key = shards.lastKey();
        } else {
            key = subMap.firstKey();
        }
        String virtualNode = shards.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * FNV1_32_HASH算法
     */
    private static int getHash(String str) {
        final int p = 16777619; // 质数系数
        int hash = (int) 2166136261L; // 种子值
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    /**
     * 负载均衡实现, 通过随机生成一个字符串来模拟请求
     * @param addressList
     * @return
     */
    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }

    /**
     * 添加节点
     * @param node
     */
    @Override
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            System.out.println("真实节点 [" + node + "] 上线添加");
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                System.out.println("虚拟节点[" + virtualNode + "] hash:" + hash + "，被添加");
            }
        }
    }

    /**
     * 删除节点
     * @param node
     */
    @Override
    public void removeNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            System.out.println("真实节点 [" + node + "] 移除成功");
            // 删除虚拟节点
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                System.out.println("虚拟节点 [" + virtualNode + "] hash: " + hash + ", 被移除");
            }
        }
    }
}
