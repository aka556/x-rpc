package org.xiaoyu.provider;

import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.api.service.UserService;
import org.xiaoyu.core.RpcApplication;
import org.xiaoyu.core.server.provider.ServiceProvider;
import org.xiaoyu.core.server.server.Impl.NettyRpcServer;
import org.xiaoyu.core.server.server.RpcServer;
import org.xiaoyu.provider.Impl.UserServiceImpl;

@Slf4j
public class ProviderTest {
    public static void main(String[] args) throws InterruptedException {
        RpcApplication.initialize(); // 初始化配置
        String ip = RpcApplication.getRpcConfig().getHost();
        int port = RpcApplication.getRpcConfig().getPort();

        // 创建userService实例
        UserService userService = new UserServiceImpl();
        ServiceProvider serviceProvider = new ServiceProvider(ip, port);
        // 发布服务器接口到服务中心
        serviceProvider.provideServiceInterface(userService);

        // 启动服务
        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(port);
        log.info("RPC 服务已启动，监听端口为 {}", port);
    }
}