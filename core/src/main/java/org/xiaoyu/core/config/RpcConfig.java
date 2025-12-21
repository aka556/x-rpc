package org.xiaoyu.core.config;

import lombok.*;
import org.xiaoyu.common.serializer.gumiSerializer.Serializer;
import org.xiaoyu.core.client.serviceCenter.balance.Impl.ConsistencyHashLoadBalance;
import org.xiaoyu.core.server.serviceRegister.Impl.ZKServiceRegister;

/**
 * 配置类
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class RpcConfig {
    // 名称
    private String name = "rpc";
    // 端口
    private int port = 9999;
    // 主机名
    private String host = "localhost";
    // 版本号
    private String version = "1.0.0";
    // 注册中心
    private String registry = new ZKServiceRegister().toString();
    // 序列化器
    private String serializer = Serializer.getSerializerByCode(3).toString();
    // 负载均衡器
    private String balance = new ConsistencyHashLoadBalance().toString();
}
