package com.wyc.orange.rpc.core.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.wyc.orange.rpc.core.config.RegistryConfig;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.RegistryServiceCache;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {

    private static Client client;

    private static KV kvClient;

    /**
     * 根节点
     */
    private final String ETCD_ROOT_PATH = "/rpc/etcd";

    /**
     * 本机注册的节点 key 集合
     */
    private final Set<String> localRegisterKeySet = new HashSet<>();

    /**
     * 注册服务缓存（消费者）
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new HashSet<>();

    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点注册的所有 registerKey
                for (String registerKey : localRegisterKeySet) {
                    try {
                        List<KeyValue>  kvs = kvClient.get(ByteSequence.from(registerKey, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        // 节点已经过期，需要重新注册
                        if (CollUtil.isEmpty(kvs)) {
                            continue;
                        }
                        KeyValue kv = kvs.get(0);
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        // 重新注册，相当于续约
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(registerKey + "续约失败", e);
                    }
                }
            }
        });

        // 支持秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        // 之前未被监听，开启监听
        boolean add = watchingKeySet.add(serviceNodeKey);
        if (add) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    switch (event.getEventType()) {
                        case DELETE:
                            // 监听的节点发生改变，清除注册服务缓存
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }

    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        // 在初始化的时候就启动心跳检测
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建租约客户端
        Lease leaseClient = client.getLeaseClient();

        // 设置租约时间
        long leaseId = leaseClient.grant(30L).get().getID();

        // 获取要存入 kv 客户端的键值对
        String registerKey = ETCD_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 设置存入 kv 客户端的存放规则
        PutOption putOption = PutOption.builder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存 localRegisterKeySet 中
        localRegisterKeySet.add(registerKey);
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        // 获取要从 kv 客户端删除的键值对
        String registerKey = ETCD_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();

        // 从 kv 客户端删除 registerKey
        kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8));

        // 从本地缓存中移除注 registerKey
        localRegisterKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 消费者先读缓存
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (!CollUtil.isEmpty(cachedServiceMetaInfoList)) {
            return cachedServiceMetaInfoList;
        }

        // 在注册中心中通过前缀搜索发现服务
        String searchPrefix = ETCD_ROOT_PATH + "/" + serviceKey + "/";
        GetOption getOption = GetOption.builder().isPrefix(true).withPrefix(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8)).build();
        try {
            List<KeyValue> kvs = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption)
                    .get()
                    .getKvs();

            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = kvs.stream().map(kv -> {
                String key = kv.getKey().toString(StandardCharsets.UTF_8);
                // 监听key的变化
                watch(key);
                String value = kv.getValue().toString(StandardCharsets.UTF_8);
                ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                return serviceMetaInfo;
            }).collect(Collectors.toList());

            // 先写缓存再返回
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        for (String registerKey : localRegisterKeySet) {
            try {
                // 从 kv 客户端中删除相应的 registerKey
                kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
