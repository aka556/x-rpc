package org.xiaoyu.customer;

import org.xiaoyu.common.util.ConfigUtil;
import org.xiaoyu.core.config.RpcConfig;

public class ConsumerTestConfig {
    public static void main(String[] args) {
        RpcConfig rpc = ConfigUtil.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
