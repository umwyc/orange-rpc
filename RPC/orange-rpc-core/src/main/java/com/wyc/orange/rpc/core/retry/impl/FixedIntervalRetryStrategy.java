package com.wyc.orange.rpc.core.retry.impl;

import com.github.rholder.retry.*;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.retry.RetryStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 重试策略 - 固定时间间隔重试
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))   // 设置重试的等待间隔时间为3s
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))   // 设置重试的最大次数为3次
                .withRetryListener(new RetryListener() {    //绑定重试的监听器记录重试次数的日志
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber() - 1);
                    }
                }).build();
        return retryer.call(callable);
    }
}
