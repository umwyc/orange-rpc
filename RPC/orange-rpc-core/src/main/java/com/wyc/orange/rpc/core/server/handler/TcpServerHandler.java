package com.wyc.orange.rpc.core.server.handler;

import com.wyc.orange.rpc.core.model.RpcRequest;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.protocol.ProtocolMessage;
import com.wyc.orange.rpc.core.protocol.ProtocolMessageDecoder;
import com.wyc.orange.rpc.core.protocol.ProtocolMessageEncoder;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageTypeEnum;
import com.wyc.orange.rpc.core.registry.LocalRegistry;
import com.wyc.orange.rpc.core.server.TcpBufferHandlerWrapper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * 使用Tcp协议的请求处理器
 */
public class TcpServerHandler implements Handler<NetSocket> {

    @Override
    public void handle(NetSocket socket) {
        // 处理请求
        TcpBufferHandlerWrapper tcpBufferHandlerWrapper = new TcpBufferHandlerWrapper(request -> {
            // 解码
            ProtocolMessage<RpcRequest> protocolMessageRequest;
            try {
                protocolMessageRequest = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(request);
            } catch (IOException e) {
                throw new RuntimeException("服务端协议解码失败");
            }
            RpcRequest rpcRequest = protocolMessageRequest.getBody();

            // 找到本地服务，并通过反射机制调用本地服务
            String serviceName = rpcRequest.getServiceName();
            Class<?> implClass = LocalRegistry.get(serviceName);
            RpcResponse rpcResponse = new RpcResponse();
            try {
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object object = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 设置rpc响应
                rpcResponse.setData(object);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                // 设置rpc响应
                e.printStackTrace();
                rpcResponse.setException(e);
                rpcResponse.setMessage(e.getMessage());
            }

            // 编码并响应
            ProtocolMessage<RpcResponse> protocolMessageResponse = new ProtocolMessage<>();
            // 将消息类型设置为响应类型
            protocolMessageRequest.getHeader().setType((byte) ProtocolMessageTypeEnum.RESPONSE.getKey());
            protocolMessageResponse.setHeader(protocolMessageRequest.getHeader());
            protocolMessageResponse.setBody(rpcResponse);

            try {
                Buffer buffer = ProtocolMessageEncoder.encode(protocolMessageResponse);
                socket.write(buffer);
            } catch (IOException e) {
                throw new RuntimeException("服务端协议编码失败");
            }

        });
        socket.handler(tcpBufferHandlerWrapper);
    }
}
