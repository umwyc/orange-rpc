package com.wyc.orange.rpc.core.bootstrap;

import com.wyc.orange.rpc.core.RpcApplication;
import com.wyc.orange.rpc.core.config.RegistryConfig;
import com.wyc.orange.rpc.core.config.RpcConfig;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;
import com.wyc.orange.rpc.core.registry.ServiceRegisterInfo;
import com.wyc.orange.rpc.core.registry.LocalRegistry;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.impl.RegistryFactory;
import com.wyc.orange.rpc.core.server.Server;
import com.wyc.orange.rpc.core.server.impl.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者启动类
 */
public class ProviderBootStrap {

    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList){
        // RPC框架全局初始化
        RpcApplication.init();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            // 本地注册
            LocalRegistry.register(serviceRegisterInfo.getServiceName(), serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceRegisterInfo.getServiceName());
            serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 启动服务器
        Server server = new VertxTcpServer();
        server.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
