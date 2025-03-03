package com.wyc.orange.rpc.core.loadbalancer.impl;

import com.wyc.orange.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希负载均衡器
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 Hash 环，用于存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点数量
     */
    private final static int VIRTUAL_NODE_NUMS = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
            return null;
        }
        // 只有一个节点，不用一致性哈希
        if(serviceMetaInfoList.size() == 1){
            return serviceMetaInfoList.get(0);
        }
        // 构建一致性 Hash 环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUMS; i++) {
                String key = serviceMetaInfo.getServiceAddress() + "#" + i;
                int hash = getHash(key);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }
        // 根据传入的参数获取 hash 值
        int hash = getHash(requestParams);
        // 获取一致性 Hash 环中大于且最接近这个 hash 值的节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
        // 没有满足条件的就选择一致性 Hash 环中的第一个节点
        if(entry == null){
            entry = virtualNodes.firstEntry();
        }
        return entry.getValue();
    }

    /**
     * 计算对象的哈希值
     *
     * @param key
     * @return
     */
    private int getHash(Object key){
        return key.hashCode();
    }
}
