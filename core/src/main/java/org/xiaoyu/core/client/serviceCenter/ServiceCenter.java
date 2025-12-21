package org.xiaoyu.core.client.serviceCenter;

import java.net.InetSocketAddress;

// 根据服务名返回地址
public interface ServiceCenter {
    // 根据服务名查询地址
    InetSocketAddress serviceDiscovery(String serviceName); // InetSocketAddress表示一个网络地址
    boolean checkRetry(String serviceName); // 判断是否可以重传
}
