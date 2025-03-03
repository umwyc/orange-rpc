package com.wyc.orange.rpc.core.registry;

import java.util.LinkedList;
import java.util.List;

/**
 * 消费者从注册中心获取到的本地缓存
 */
public class RegistryServiceCache {

    /**
     * 本地缓存
     */
    private List<ServiceMetaInfo> serviceCache = new LinkedList<ServiceMetaInfo>();

    /**
     * 读取本地缓存
     * @return
     */
    public List<ServiceMetaInfo> readCache(){
        return this.serviceCache;
    }

    /**
     * 写本地缓存
     * @param newServiceCache
     */
    public void writeCache(List<ServiceMetaInfo> newServiceCache){
        this.serviceCache = newServiceCache;
    }

    /**
     * 清除本地缓存
     */
    public void clearCache(){
        this.serviceCache = null;
    }
}
