package org.xiaoyu.core.server.rateLimit.Impl;

import org.xiaoyu.core.server.rateLimit.RateLimit;

public class TokenBuketRateLimitImpl implements RateLimit {
    private final int rate; // 令牌桶的速率
    private final int capacity; // 令牌桶的容量
    private volatile int curCapacity; // 令牌桶的当前容量
    private volatile long timestamp = System.currentTimeMillis(); // 上次请求的时间戳

    public TokenBuketRateLimitImpl(int rate, int capacity) {
        this.rate = rate; // 令牌产生的速率
        this.capacity = capacity; // 令牌桶的最大容量
        this.curCapacity = capacity; // 初始化当前容量为最大容量
        this.timestamp = System.currentTimeMillis(); // 初始化上次请求的时间戳
    }

    @Override
    public boolean getToken() {
        synchronized (this) {
            if (curCapacity > 0) {
                curCapacity--;
                return true;
            }
        }

        // 没有令牌, 计算生成令牌的情况
        long current = System.currentTimeMillis();
        // 计算与上次请求的间隔时间
        if ((current - timestamp) >= rate) {
            int generatedTokens = (int) ((current - timestamp) / rate);
            if (generatedTokens > 1) {
                // 生成令牌数量, 减1是因为已经生成了一个令牌
                curCapacity = Math.min(capacity, capacity + generatedTokens - 1);
            }

            // 更新时间戳
            timestamp = current;
            return true; // 表示可以消费一个令牌
        }
        return false; // 无法获取令牌
    }
}
