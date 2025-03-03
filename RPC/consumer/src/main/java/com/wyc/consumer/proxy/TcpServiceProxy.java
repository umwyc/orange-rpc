package com.wyc.consumer.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.wyc.orange.rpc.core.RpcApplication;
import com.wyc.orange.rpc.core.constants.ProtocolConstant;
import com.wyc.orange.rpc.core.constants.RpcConstant;
import com.wyc.orange.rpc.core.loadbalancer.LoadBalancer;
import com.wyc.orange.rpc.core.loadbalancer.impl.LoadBalancerFactory;
import com.wyc.orange.rpc.core.model.RpcRequest;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.registry.ServiceMetaInfo;
import com.wyc.orange.rpc.core.protocol.ProtocolMessage;
import com.wyc.orange.rpc.core.protocol.ProtocolMessageDecoder;
import com.wyc.orange.rpc.core.protocol.ProtocolMessageEncoder;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageSerializerEnum;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageStatusEnum;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageTypeEnum;
import com.wyc.orange.rpc.core.registry.Registry;
import com.wyc.orange.rpc.core.registry.impl.RegistryFactory;
import com.wyc.orange.rpc.core.retry.RetryStrategy;
import com.wyc.orange.rpc.core.retry.impl.RetryStrategyFactory;
import com.wyc.orange.rpc.core.server.TcpBufferHandlerWrapper;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 使用 tcp 协议进行通信的代理类
 */
public class TcpServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ProtocolMessage<RpcRequest> protocolMessageRequest = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        // 构造请求头
        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        // 生成全局请求ID
        header.setRequestId(IdUtil.getSnowflakeNextId());

        // 设置rpc请求
        Class<?> serviceClass = method.getDeclaringClass();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceClass.getName())
                .serviceVersion(RpcConstant.DEFAULT_SERVICE_VERSION)
                .parameterTypes(method.getParameterTypes())
                .methodName(method.getName())
                .args(args)
                .build();

        // 编码
        protocolMessageRequest.setHeader(header);
        protocolMessageRequest.setBody(rpcRequest);

        // 注册中心服务发现
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceClass.getName());
        serviceMetaInfo.setServiceVersion(RpcApplication.getRpcConfig().getVersion());
        Registry registry = RegistryFactory.getInstance(RpcApplication.getRpcConfig().getRegistryConfig().getRegistry());
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());

        System.out.println("服务发现:" + serviceMetaInfoList);

        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }

        // 使用负载均衡器帮助选择节点
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(RpcApplication.getRpcConfig().getLoadBalancer());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(Map.ofEntries(
                Map.entry("address", serviceMetaInfo.getServiceAddress())
        ), serviceMetaInfoList);

        System.out.println("服务选择:" + selectedServiceMetaInfo);

        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP客户端
        NetClient netClient = vertx.createNetClient();

        // 获取重试策略
        RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcApplication.getRpcConfig().getRetryStrategy());

        // 启动重试策略并发送请求
        RpcResponse rpcResponse = retryStrategy.doRetry(() -> {
            // 连接至TCP客户端并发送请求
            CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
            netClient.connect(selectedServiceMetaInfo.getServicePort(), selectedServiceMetaInfo.getServiceHost(), result -> {
                if (result.succeeded()) {
                    System.out.println("客户端启动成功");
                    NetSocket socket = result.result();
                    // 先编码发送请求
                    try {
                        socket.write(ProtocolMessageEncoder.encode(protocolMessageRequest));
                    } catch (IOException e) {
                        throw new RuntimeException("客户端协议编码失败");
                    }

                    // 处理响应结果
                    TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(response -> {
                        try {
                            ProtocolMessage<RpcResponse> protocolMessageResponse = (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(response);
                            responseFuture.complete(protocolMessageResponse.getBody());
                        } catch (IOException e) {
                            throw new RuntimeException("客户端协议解码失败");
                        }
                    });
                    socket.handler(tcpBufferHandlerWrapper);
                } else {
                    System.out.println("客户端启动失败");
                }
            });
            RpcResponse tmpRpcResponse = responseFuture.get();
            // 关闭连接
            netClient.close();
            return tmpRpcResponse;
        });

        // 返回方法执行的结果
        return rpcResponse.getData();
    }
}
