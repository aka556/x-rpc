package org.xiaoyu.core;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.util.ConfigUtil;
import org.xiaoyu.core.config.RpcConfig;
import org.xiaoyu.core.config.RpcConstant;

@Slf4j
public class RpcApplication {
    private static volatile RpcConfig rpcConfigInstance;

    // 初始化配置
    public static void initialize(RpcConfig customConfig) {
        rpcConfigInstance = customConfig;
        log.info("Rpc框架初始化, 配置={}", customConfig);
    }

    public static void initialize() {
        RpcConfig customRpcConfig;

        try {
            customRpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.CONFIG_FIFE_PREFIX);
            log.info("加载配置文件成功, 配置={}", customRpcConfig); // 加载成功日志信息
        } catch (Exception e) {
            log.warn("加载配置文件失败, 使用默认配置");
            customRpcConfig = new RpcConfig();
        }

        initialize(customRpcConfig);
    }

    public static RpcConfig getRpcConfig() {
        if (rpcConfigInstance == null) {
            synchronized (RpcApplication.class) { // 同步锁，防止多线程重复初始化
                if (rpcConfigInstance == null) {
                    initialize(); // 初始化配置, 确保在第一次调用时初始化
                }
            }
        }
        return rpcConfigInstance;
    }
}
