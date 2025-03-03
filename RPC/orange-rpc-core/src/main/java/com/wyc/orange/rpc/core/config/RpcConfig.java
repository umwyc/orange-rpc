package com.wyc.orange.rpc.core.config;

import com.wyc.orange.rpc.core.loadbalancer.LoadBalancerKeys;
import com.wyc.orange.rpc.core.retry.RetryStrategyKeys;
import com.wyc.orange.rpc.core.serializer.SerializerKeys;
import lombok.Data;

/**
 * Rpc框架全局配置类
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "wyc-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器的主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器使用的端口号
     */
    private Integer serverPort = 8080;

    /**
     * 是否启动mock
     */
    private boolean mock = false;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 注册中心配置
     */
    RegistryConfig registryConfig = new RegistryConfig();

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 失败重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;
}
