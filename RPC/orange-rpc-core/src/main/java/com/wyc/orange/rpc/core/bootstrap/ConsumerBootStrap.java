package com.wyc.orange.rpc.core.bootstrap;

import com.wyc.orange.rpc.core.RpcApplication;

/**
 * 服务消费者启动类
 */
public class ConsumerBootStrap {

    /**
     * 初始化
     */
    public static void init(){
        // RPC框架全局配置
        RpcApplication.init();
    }
}
