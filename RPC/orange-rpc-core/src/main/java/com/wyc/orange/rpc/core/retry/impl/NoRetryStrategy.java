package com.wyc.orange.rpc.core.retry.impl;

import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.retry.RetryStrategy;

import java.util.concurrent.Callable;

/**
 * 重试策略 - 不重试
 */
public class NoRetryStrategy implements RetryStrategy {

    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
