package com.wyc.orange.rpc.core.retry.impl;

import com.wyc.orange.rpc.core.retry.RetryStrategy;
import com.wyc.orange.rpc.core.spi.SpiLoader;

/**
 * 重试策略工厂
 */
public class RetryStrategyFactory {

    static{
        SpiLoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试策略
     */
    private final RetryStrategy retryStrategy = new NoRetryStrategy();

    /**
     * 从工厂中获取实例
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key){
        return SpiLoader.getInstance(RetryStrategy.class, key);
    }
}
