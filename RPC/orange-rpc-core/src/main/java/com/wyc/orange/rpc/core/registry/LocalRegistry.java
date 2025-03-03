package com.wyc.orange.rpc.core.registry;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地缓存服务提供类
 */
public class LocalRegistry {

    /**
     * 存储注册信息（接口类的全限定名 => 本地接口的实现类）
     */
    private static final ConcurrentHashMap<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param implClass
     */
    public static void register(String serviceName, Class<?> implClass){
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务
     *
     * @param serviceName
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 删除服务
     *
     * @param serviceName
     */
    public static void remove(String serviceName){
        map.remove(serviceName);
    }
}
