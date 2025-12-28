package org.xiaoyu.core.client.RpcClient.Impl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.common.trace.TraceContext;
import org.xiaoyu.core.client.RpcClient.RpcClient;
import org.xiaoyu.core.client.netty.handler.MDCChannelHandler;
import org.xiaoyu.core.client.netty.nettyInitializer.NettyClientInitializer;
import org.xiaoyu.core.client.serviceCenter.ServiceCenter;
import org.xiaoyu.core.client.serviceCenter.ZKServerCenter;

import java.net.InetSocketAddress;
import java.util.Map;

@Slf4j
public class NettyRpcClient implements RpcClient {
    private static final Bootstrap bootstrap; // netty用户启动客户端的对象
    private static final EventLoopGroup eventLoopGroup; // netty的线程池，用于处理I/O操作

    private final InetSocketAddress address;

    public NettyRpcClient(InetSocketAddress serviceAddress) {
        this.address = serviceAddress;
    }

    // 客户端初始化
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        // 配置netty对消息的处理机制
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {
        Map<String, String> mdcContextMap = TraceContext.getCopy();

        // 从注册中心获取host地址以及port端口号
        if (address == null) {
            log.error("服务发现失败，返回地址为null");
            return RpcResponse.fail("服务未发现, 地址为null");
        }

        String host = address.getHostName();
        int port = address.getPort();

        try {
            // 创建一个channelFuture对象，代表这一个操作事件，sync方法表示堵塞直到connect完成
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            // channel表示一个连接的单位，类似socket
            Channel channel = channelFuture.channel();
            // 将trace上下文保存到channel属性中
            channel.attr(MDCChannelHandler.TRACE_CONTEXT_KEY).set(mdcContextMap);

            // 发送数据
            channel.writeAndFlush(request);
            // sync()同步堵塞获取结果
            channel.closeFuture().sync();
            // 阻塞的获得结果，通过给channel设计别名，获取特定名字下的channel中的内容（这个在handler中设置）
            // AttributeKey是，线程隔离的，不会由线程安全问题。
            // 当前场景下选择堵塞获取结果
            // 其它场景也可以选择添加监听器的方式来异步获取结果 channelFuture.addListener...
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
            RpcResponse response = channel.attr(key).get();
            if (response == null) {
                log.error("服务调用失败，返回结果为null");
                return RpcResponse.fail("服务响应为空");
            }

            log.info("接收到的响应：{}", response);
            return response;
        } catch (InterruptedException e) {
            log.error("请求被中断，发送请求失败: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("发送请求失败: {}", e.getMessage(), e);
        } finally {

        }
        return RpcResponse.fail("请求失败");
    }

    // 关闭客户端
    public void close() {
        try {
            eventLoopGroup.shutdownGracefully().sync();
        } catch (Exception e) {
            log.error("关闭客户端时失败: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }
}
