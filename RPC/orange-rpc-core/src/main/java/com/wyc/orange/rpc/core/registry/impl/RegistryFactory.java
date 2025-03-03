package com.wyc.orange.rpc.core.registry.impl;

import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.spi.SpiLoader;

/**
 * 注册中心工厂
 */
public class RegistryFactory {

    static{
        SpiLoader.load(Registry.class);
    }

    /**
     * 默认的注册中心
     */
    private static Registry registry = new EtcdRegistry();

    /**
     * 获取实例
     * @param key
     * @return
     */
    public static Registry getInstance(String key){
        return SpiLoader.getInstance(Registry.class, key);
    }
}
