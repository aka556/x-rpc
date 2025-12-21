package org.xiaoyu.core.client.circuitBreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakProvider {
    // 用一个map存储每个服务的熔断器实例, key为服务名， value为熔断器实例
    // 使用线程安全的集合结构
    private Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    // 根据服务名获取熔断器实例
    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakerMap.computeIfAbsent(serviceName, k -> {
            log.info("服务 [{}]不存在熔断器，创建新的熔断器实例", serviceName);
            // 创建并返回熔断器
            return new CircuitBreaker(1, 0.5, 10000);
        });
    }
}
