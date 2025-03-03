package com.wyc.orange.rpc.core.loadbalancer.impl;

import com.wyc.orange.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.size() == 0){
            return null;
        }
        // 当前只有一个节点，不用随机
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }
        return serviceMetaInfoList.get(new Random().nextInt(serviceMetaInfoList.size()));
    }
}
