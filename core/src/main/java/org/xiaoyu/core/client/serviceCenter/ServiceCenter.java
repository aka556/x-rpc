package org.xiaoyu.core.client.serviceCenter;

import org.xiaoyu.common.message.RpcRequest;

import java.net.InetSocketAddress;

// 根据服务名返回地址
public interface ServiceCenter {
    // 根据服务名查询地址
    InetSocketAddress serviceDiscovery(RpcRequest request); // InetSocketAddress表示一个网络地址
    boolean checkRetry(InetSocketAddress serviceAddress, String methodSignature); // 判断是否可以重传
    // 关闭
    void close();
}
