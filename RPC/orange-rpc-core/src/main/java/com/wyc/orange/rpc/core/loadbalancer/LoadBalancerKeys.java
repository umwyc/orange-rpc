package com.wyc.orange.rpc.core.loadbalancer;

/**
 * 支持多种负载均衡机制
 */
public interface LoadBalancerKeys {

    String ROUND_ROBIN = "roundRobin";

    String RANDOM = "random";

    String CONSISTENT_HASH = "consistentHash";
}
