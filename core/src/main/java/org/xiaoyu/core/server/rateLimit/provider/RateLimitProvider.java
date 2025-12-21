package org.xiaoyu.core.server.rateLimit.provider;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.core.server.rateLimit.Impl.TokenBuketRateLimitImpl;
import org.xiaoyu.core.server.rateLimit.RateLimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RateLimitProvider {
    private Map<String, RateLimit> rateLimitMap = new ConcurrentHashMap<>();

    // 默认限流桶和令牌生成速率
    private static final int DEFAULT_CAPACITY = 100;
    private static final int DEFAULT_RATE = 10;

    /**
     * 根据接口名称获取对应的速率限制器实例。
     * 如果该接口的速率限制器实例不存在，则会创建一个新的实例并返回。
     *
     * @param interfaceName 接口名称
     * @return 对应接口的速率限制器实例
     */
    public RateLimit getRateLimit(String interfaceName) {
        RateLimit rateLimit = new TokenBuketRateLimitImpl(DEFAULT_CAPACITY, DEFAULT_RATE);
        log.info("为接口[{}] 创建了新的限流策略: {}", interfaceName, rateLimit);
        return rateLimit;
    }
}
