package org.xiaoyu.core.server.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.core.server.provider.ServiceProvider;
import org.xiaoyu.core.server.rateLimit.RateLimit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@AllArgsConstructor
@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private ServiceProvider serviceProvider;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        // 非空判断
        if (request == null) {
            log.error("接收到非法请求，RpcRequest为空");
            return;
        }

        RpcResponse response = getResponse(request);
        ctx.writeAndFlush(response);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private RpcResponse getResponse(RpcRequest rpcRequest) {
        // 获取服务名
        String interfaceName = rpcRequest.getInterfaceName();
        // 接口限流降级
        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(interfaceName);
        if (!rateLimit.getToken()) {
            log.warn("接口: {} 已限流", interfaceName);
            return RpcResponse.fail("服务已限流, 接口" + interfaceName + "当前无法处理请求，请稍后再试");
        }

        // 获取服务实现类
        Object service = serviceProvider.getService(interfaceName);
        // 反射调用
        Method method = null;
        try {
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamsType());
            Object invoke = method.invoke(service,rpcRequest.getParams());
            return RpcResponse.success(invoke);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("方法执行错误，接口: {}, 方法: {}", interfaceName, rpcRequest.getMethodName(), e);
            return RpcResponse.fail("Netty服务端方法执行失败");
        }
    }
}
