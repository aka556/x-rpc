package org.xiaoyu.core.server.provider;

import org.xiaoyu.core.server.rateLimit.provider.RateLimitProvider;
import org.xiaoyu.core.server.serviceRegister.Impl.ZKServiceRegister;
import org.xiaoyu.core.server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private Map<String, Object> interfaceProvider;

    private String host;
    private int port;

    // 注册服务
    private ServiceRegister serviceRegister;
    // 限流器
    private RateLimitProvider rateLimitProvider;

    public ServiceProvider(String host, int port) {
        this.host = host;
        this.port = port;
        this.serviceRegister = new ZKServiceRegister();
        this.interfaceProvider = new HashMap<>();
        this.rateLimitProvider = new RateLimitProvider();
    }

    // 本地服务
    public void provideServiceInterface(Object service, boolean canRetry) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();

        for (Class<?> clazz : interfaceName) {
            interfaceProvider.put(clazz.getName(), service);
            serviceRegister.register(clazz.getName(), new InetSocketAddress(host, port), canRetry);
        }
    }

    public Object getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }

    public RateLimitProvider getRateLimitProvider() { return rateLimitProvider; }
}
