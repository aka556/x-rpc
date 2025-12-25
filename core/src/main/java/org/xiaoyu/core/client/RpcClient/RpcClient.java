package org.xiaoyu.core.client.RpcClient;

import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;

public interface RpcClient {
    // 定义底层通信方法
    RpcResponse sendRequest(RpcRequest request);
    // 关闭客户端
    void close();
}
