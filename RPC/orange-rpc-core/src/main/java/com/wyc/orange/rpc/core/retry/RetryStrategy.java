package com.wyc.orange.rpc.core.retry;

import com.wyc.orange.rpc.core.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试机制接口（消费者使用）
 */
public interface RetryStrategy {

    /**
     * 重试
     *
     * @param callable
     * @return
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;

}
