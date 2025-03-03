package com.wyc.orange.rpc.core.registry;

/**
 * 支持多种注册中心
 */
public interface RegistryKeys {

    /**
     * etcd 注册中心
     */
    String ETCD = "etcd";

    /**
     * zookeeper 注册中心
     */
    String ZOOKEEPER = "zookeeper";
}
