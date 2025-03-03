package com.wyc.provider;

import com.wyc.common.service.UserService;
import com.wyc.provider.impl.UserServiceImpl;
import com.wyc.orange.rpc.core.RpcApplication;
import com.wyc.orange.rpc.core.config.RegistryConfig;
import com.wyc.orange.rpc.core.config.RpcConfig;
import com.wyc.orange.rpc.core.registry.LocalRegistry;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.impl.RegistryFactory;
import com.wyc.orange.rpc.core.server.Server;
import com.wyc.orange.rpc.core.server.impl.VertxTcpServer;

/**
 * 提供者启动类
 */
public class ProviderByTcp {
    public static void main(String[] args) {
        // RPC框架初始化
        RpcApplication.init();

        // 注册本地服务
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 上传服务至注册中心
        String serviceName = UserService.class.getName();
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceVersion(rpcConfig.getVersion());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        // 启动服务器
        Server server = new VertxTcpServer();
        server.doStart(rpcConfig.getServerPort());
    }
}
