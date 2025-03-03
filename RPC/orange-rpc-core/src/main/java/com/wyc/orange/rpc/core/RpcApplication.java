package com.wyc.orange.rpc.core;

import com.wyc.orange.rpc.core.config.RegistryConfig;
import com.wyc.orange.rpc.core.config.RpcConfig;
import com.wyc.orange.rpc.core.constants.RpcConstant;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.impl.RegistryFactory;
import com.wyc.orange.rpc.core.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Rpc框架全局配置加载器
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    /**
     * 框架初始化
     */
    public static void init(RpcConfig newRpcConfig){
        rpcConfig = newRpcConfig;
        // 更新配置的时候记录记录日志
        log.info("rpc init, config = {}", newRpcConfig.toString());
        // 注册中心初始化
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        registry.init(registryConfig);
        log.info("registry init, config = {}", registryConfig);
        // 创建JVM ShutdownHook，设置程序退出的时候自动销毁当前主机的节点
        Runtime.getRuntime().addShutdownHook(new Thread(() -> registry.destroy()));
    }

    /**
     * 支持自定义完成框架初始化
     */
    public static void init(){
        RpcConfig newRpcConfig;
        try {
            // 成功读取application.properties配置文件
            newRpcConfig = ConfigUtil.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            // 读取失败，选择默认配置
            e.printStackTrace();
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置（第一次获取配置会触发 RpcConfig 的加载）
     *
     * @return
     */
    public static RpcConfig getRpcConfig(){
        if(rpcConfig == null){
            synchronized (RpcApplication.class){
                if(rpcConfig == null){
                    init();
                }
            }
        }
        return rpcConfig;
    }
}
