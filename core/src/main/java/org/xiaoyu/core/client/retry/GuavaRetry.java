package org.xiaoyu.core.client.retry;

import com.github.rholder.retry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xiaoyu.common.message.RpcRequest;
import org.xiaoyu.common.message.RpcResponse;
import org.xiaoyu.core.client.RpcClient.RpcClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class GuavaRetry {
    private static final Logger log = LoggerFactory.getLogger(GuavaRetry.class);
    // 发送请求
    private RpcClient rpcClient;

    // 发送请求的重试机制
    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfException()
                // 当出现请求异常或是返回码为500时触发重试
                .retryIfResult(response -> Objects.equals(response.getCode(), 500))
                // 间隔2秒
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                // 最多重试3次
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 配置监听器
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        System.out.println("RetryListener: 第" + attempt.getAttemptNumber() + "次重试");
                    }
                }).build();
        try {
            return retryer.call(() -> rpcClient.sendRequest(request));
        } catch (Exception e) {
            log.info("重试失败，请求{}发生异常", request.getMethodName(),  e);
        }
        // 返回失败消息
        return RpcResponse.fail("重试失败，所有重试尝试已结束");
    }
}
