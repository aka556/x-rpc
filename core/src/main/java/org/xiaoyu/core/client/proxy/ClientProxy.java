package org.xiaoyu.core.client.proxy;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.common.message.RequestType;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.core.client.RpcClient.Impl.NettyRpcClient;
import org.xiaoyu.core.client.RpcClient.RpcClient;
import org.xiaoyu.core.client.circuitBreaker.CircuitBreakProvider;
import org.xiaoyu.core.client.circuitBreaker.CircuitBreaker;
import org.xiaoyu.core.client.retry.GuavaRetry;
import org.xiaoyu.core.client.serviceCenter.ServiceCenter;
import org.xiaoyu.core.client.serviceCenter.ZKServerCenter;
import org.xiaoyu.core.trace.Interceptor.ClientTraceInterceptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

// 传入参数service接口的class对象，反射封装成一个request
// RPCClientProxy类中需要加入一个RPCClient类变量即可， 传入不同的client(simple,netty), 即可调用公共的接口sendRequest发送请求
@Slf4j
public class ClientProxy implements InvocationHandler {
    private RpcClient rpcClient;
    private ServiceCenter serviceCenter;
    private CircuitBreakProvider circuitBreakProvider;
    public ClientProxy() throws InterruptedException {
        serviceCenter = new ZKServerCenter();
       circuitBreakProvider = new CircuitBreakProvider();
    }

    // 动态代理
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // trace记录
        ClientTraceInterceptor.beforeInvoke();

        // 构建request
        RpcRequest request = RpcRequest.builder()
                .requestType(RequestType.NORMAL)
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args).paramsType(method.getParameterTypes())
                .build();

        // 添加熔断器
        CircuitBreaker circuitBreaker = circuitBreakProvider.getCircuitBreaker(method.getName());
        // 判断熔断器是否允许通过
        if (!circuitBreaker.allowRequest()) {
            log.warn("熔断器开启，请求被拒绝: {}", request);
            return null;
        }

        // 数据传输
        RpcResponse response;
        // 根据方法签名判断是否需要重试
        String methodSignature = getSignature(request.getInterfaceName(), method);
        InetSocketAddress serviceAddress = serviceCenter.serviceDiscovery(request);
        rpcClient = new NettyRpcClient(serviceAddress);

        if (serviceCenter.checkRetry(serviceAddress, methodSignature)) {
            try {
                log.info("使用重试机制调用服务: {}", methodSignature);
                // 调用retry框架进行重试
                response = new GuavaRetry().sendServiceWithRetry(request, rpcClient);
            } catch (Exception e) {
                log.error("重试失败，方法签名: {}", methodSignature, e);
                circuitBreaker.recordFailure();
                throw e;
            }
        } else {
            // 不需要重试，只调用一次
            response = rpcClient.sendRequest(request);
        }

        if (response != null) {
            // 记录response状态
            if (response.getCode() == 200) {
                circuitBreaker.recordSuccess();
            } else if (response.getCode() == 500) {
                circuitBreaker.recordFailure();
            }
            log.info("收到响应: {} 状态码: {}", request.getInterfaceName(), response.getCode());
        }

        // trace上报
        ClientTraceInterceptor.afterInvoke(method.getName());

        return response != null ? response.getData() : null;
    }

    public <T> T getProxy(Class<T> clazz) {
        Object o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, this);
        return (T) o;
    }

    // 根据接口名字和方法获取方法签名
    public String getSignature(String interfaceName, Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(interfaceName).append("#").append(method.getName()).append("(");
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(parameterTypes[i].getName());
            if (i < parameterTypes.length - 1) {
                sb.append(",");
            } else {
                sb.append(")");
            }
        }
        return sb.toString();
    }

    //关闭创建的资源
    //注：如果在需要C-S保持长连接的场景下无需调用close方法
    public void close(){
        rpcClient.close();
        serviceCenter.close();
    }
}
