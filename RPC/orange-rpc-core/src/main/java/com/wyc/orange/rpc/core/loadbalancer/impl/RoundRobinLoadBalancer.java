package com.wyc.orange.rpc.core.loadbalancer.impl;

import com.wyc.orange.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {

    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
            return null;
        }
        // 当前只有一个节点，不用轮询
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }
        return serviceMetaInfoList.get(currentIndex.getAndIncrement() % serviceMetaInfoList.size());
    }
}
