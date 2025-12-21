package org.xiaoyu.core.client.circuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {
    private CircuitBreakerState state = CircuitBreakerState.CLOSED; // 默认为关闭状态
    private AtomicInteger failureCount = new AtomicInteger(0); // 默认失败次数为0
    private AtomicInteger successCount = new AtomicInteger(0); // 默认成功次数为0
    private AtomicInteger requestCount = new AtomicInteger(0); // 默认请求总次数为0

    private final int failureThreshold; // 失败阈值
    private final double halfOpenSuccessRate; // 半开状态下的成功率阈值
    private final long resetTimePeriod; // 重置时间间隔
    private long lastFailureTime = 0; // 最后一次失败时间

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long resetTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.resetTimePeriod = resetTimePeriod;
    }

    // 根据当前熔断状器状态是否允许请求
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        System.out.println("熔断swtich之前!!!!!!! failureNum==" + failureCount);
        switch (state) {
            case OPEN:
                if (currentTime - lastFailureTime > resetTimePeriod) {
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts(); // 重置计数
                    return true; // 允许请求
                }
                System.out.println("熔断失效");
                return false; // 继续熔断
            case HALF_OPEN:
                requestCount.incrementAndGet(); // 在半开状态下记录请求
                return true; // 允许请求
            case CLOSED:
            default:
                return true; // 服务正常，拒绝请求
        }
    }

    // 记录一次成功的请求
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            successCount.incrementAndGet();
            if (successCount.get() >= requestCount.get() * halfOpenSuccessRate) {
                state = CircuitBreakerState.CLOSED;
                resetCounts(); // 重置计数
            } else {
                resetCounts(); // 不是搬开状态, 重置计数
            }
        }
    }

    // 记录一次失败的请求
    public synchronized void recordFailure() {
       failureCount.incrementAndGet();
       lastFailureTime = System.currentTimeMillis(); // 记录失败时间

        if (state == CircuitBreakerState.HALF_OPEN) {
            state = CircuitBreakerState.OPEN; // 半开失败，切换到打开状态
        } else if (failureCount.get() >= failureThreshold) {
            state = CircuitBreakerState.OPEN; // 失败超过阈值，切换到打开状态
        }
    }

    // 重置计数
    private void resetCounts() {
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }
}

enum CircuitBreakerState {
    // 状态
    CLOSED,
    OPEN,
    HALF_OPEN
}
