package com.wyc.consumer.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.wyc.orange.rpc.core.RpcApplication;
import com.wyc.orange.rpc.core.config.RpcConfig;
import com.wyc.orange.rpc.core.model.RpcRequest;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;
import com.wyc.orange.rpc.core.registry.impl.RegistryFactory;
import com.wyc.orange.rpc.core.serializer.Serializer;
import com.wyc.orange.rpc.core.serializer.impl.SerializerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 使用 Http 协议进行通信的代理类
 */
public class HttpServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args){
        // 指定序列化器（默认是 jdk）
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        final Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());

        // 构造 rpc 请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();


        try {
            // 序列化 rpc 请求
            byte[] bytes = serializer.serialize(rpcRequest);
            byte[] result;

            // 从注册中心获取服务提供者
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcApplication.getRpcConfig().getVersion());
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if(CollUtil.isEmpty(serviceMetaInfoList)){
                throw new RuntimeException("暂无服务地址");
            }

            // 暂时取第一个
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);

            // 发送 Http 请求并获取 Http 响应（rpc 请求被携带在了请求体中）
            HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())
                    .body(bytes)
                    .execute();
            result = httpResponse.bodyBytes();

            // 构造 rpc 响应并返回方法执行结果
            RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
            return rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
