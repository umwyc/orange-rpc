package com.wyc.orange.rpc.core.server.handler;

import com.wyc.orange.rpc.core.RpcApplication;
import com.wyc.orange.rpc.core.registry.LocalRegistry;
import com.wyc.orange.rpc.core.serializer.Serializer;
import com.wyc.orange.rpc.core.model.RpcRequest;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.serializer.impl.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 使用Http协议的请求处理器
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest request) {
        // 获取序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 记录日志
        System.out.println("Received method: " + request.method() + " " + request.uri());

        // 处理请求体
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            if (rpcRequest == null) {
                rpcResponse.setMessage("request is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }

            try {
                String serviceName = rpcRequest.getServiceName();
                String methodName = rpcRequest.getMethodName();
                Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
                Object[] args = rpcRequest.getArgs();
                // 在本地缓存查询服务提供类
                Class<?> service = LocalRegistry.get(serviceName);
                // 找到服务类中的具体某一项方法
                Method method = service.getMethod(methodName, parameterTypes);
                // 通过反射机制调用方法并获取调用结果
                Object object = method.invoke(service.newInstance(), args);
                rpcResponse.setData(object);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 响应
            doResponse(request, rpcResponse, serializer);
        });

    }

    /**
     * 返回响应给调用客户端
     *
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");
        try {
            httpServerResponse.end(Buffer.buffer(serializer.serialize(rpcResponse)));
        } catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
