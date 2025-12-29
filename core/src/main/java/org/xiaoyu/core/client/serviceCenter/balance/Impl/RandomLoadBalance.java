package org.xiaoyu.core.client.serviceCenter.balance.Impl;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.core.client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 随机数负载均衡
 */
@Slf4j
public class RandomLoadBalance implements LoadBalance {
    private final static Random random = new Random();

    private final List<String> addressList = new CopyOnWriteArrayList<>();

    @Override
    public String balance(List<String> addressList) {
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }

        int choose = random.nextInt(addressList.size());
        log.info("随机数负载均衡选择了第{}号服务器，地址为{}", choose, addressList.get(choose));
        return addressList.get(choose);
    }

    @Override
    public void addNode(String node) {
        // 动态添加节点
        addressList.add(node);
        log.info("随机数负载均衡添加了服务器{}", node);
    }

    @Override
    public void removeNode(String node) {
        // 动态删除节点
        addressList.remove(node);
        log.info("随机数负载均衡移除了服务器{}", node);
    }
}
