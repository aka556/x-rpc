package org.xiaoyu.core.client.serviceCenter.balance;

import java.util.List;

/**
 * 负载均衡接口, 解决单节点流量访问过大问题
 */
public interface LoadBalance {
    // 实现具体算法，返回节点地址
    String balance(List<String> addressList);
    // 添加节点
    void addNode(String node);
    // 删除节点
    void removeNode(String node);
}
