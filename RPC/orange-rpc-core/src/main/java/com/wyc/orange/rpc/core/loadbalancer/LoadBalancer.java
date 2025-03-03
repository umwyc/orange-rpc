package com.wyc.orange.rpc.core.loadbalancer;

import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡器接口（提供者使用）
 */
public interface LoadBalancer {

    ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList);

}
