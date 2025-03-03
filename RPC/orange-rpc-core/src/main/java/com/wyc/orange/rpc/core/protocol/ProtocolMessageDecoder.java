package com.wyc.orange.rpc.core.protocol;

import com.wyc.orange.rpc.core.constants.ProtocolConstant;
import com.wyc.orange.rpc.core.model.RpcRequest;
import com.wyc.orange.rpc.core.model.RpcResponse;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageSerializerEnum;
import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageTypeEnum;
import com.wyc.orange.rpc.core.serializer.Serializer;
import com.wyc.orange.rpc.core.serializer.impl.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 自定义消息解码器
 */
public class ProtocolMessageDecoder {

    public static ProtocolMessage<?> decode(Buffer buffer) throws IOException {
        // 校验魔数
        byte magic = buffer.getByte(0);
        if(magic != ProtocolConstant.PROTOCOL_MAGIC){
            throw new RuntimeException("消息 magic 非法");
        }
        // 分别从指定位置读出 buffer
        ProtocolMessage.Header header = new ProtocolMessage.Header();
        header.setMagic(magic);
        header.setVersion(buffer.getByte(1));
        header.setSerializer(buffer.getByte(2));
        header.setType(buffer.getByte(3));
        header.setStatus(buffer.getByte(4));
        header.setRequestId(buffer.getByte(5));
        header.setBodyLength(buffer.getInt(13));

        // 尝试获取消息中指定的序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(buffer.getByte(2));
        if(serializerEnum == null){
            throw new RuntimeException("序列化器不存在");
        }

        // 将消息体中的消息解码
        int bodyLength = buffer.getInt(13);
        byte[] bodyBytes = buffer.getBytes(17, 17 + bodyLength);
        byte type = buffer.getByte(3);
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        ProtocolMessageTypeEnum typeEnum = ProtocolMessageTypeEnum.getEnumByKey(type);
        if(typeEnum == null){
            throw new RuntimeException("序列化的消息类型不存在");
        }
        switch (typeEnum) {
            case REQUEST:
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                return new ProtocolMessage<>(header, rpcRequest);
            case RESPONSE:
                RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                return new ProtocolMessage<>(header, rpcResponse);
            case HEART_BEAT:
            case OTHERS:
            default:
                throw new RuntimeException("暂不支持该消息类型");
        }
    }
}
