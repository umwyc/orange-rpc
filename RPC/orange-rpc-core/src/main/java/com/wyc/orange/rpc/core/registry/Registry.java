package com.wyc.orange.rpc.core.registry;

import java.util.List;
import com.wyc.orange.rpc.core.config.RegistryConfig;

public interface Registry {


    /**
     *
     * 心跳检测（提供者）
     */
    void heartBeat();

    /**
     *
     * 监听
     * @param serviceNodeKey
     */
    void watch(String serviceNodeKey);

    /**
     *
     * 初始化注册中心
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);

    /**
     *
     * 注册服务
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     *
     * 注销服务
     * @param serviceMetaInfo
     */
    void unregister(ServiceMetaInfo serviceMetaInfo);

    /**
     *
     * 服务发现
     * @param serviceKey
     * @return
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     *
     * 服务销毁
     */
    void destroy();
}
