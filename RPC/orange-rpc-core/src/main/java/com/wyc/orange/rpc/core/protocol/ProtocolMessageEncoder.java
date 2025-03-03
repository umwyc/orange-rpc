package com.wyc.orange.rpc.core.protocol;

import com.wyc.orange.rpc.core.protocol.enums.ProtocolMessageSerializerEnum;
import com.wyc.orange.rpc.core.serializer.Serializer;
import com.wyc.orange.rpc.core.serializer.impl.SerializerFactory;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;

/**
 * 自定义消息编码器
 */
public class ProtocolMessageEncoder {

    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if(protocolMessage == null || protocolMessage.getHeader() == null){
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        Buffer buffer = Buffer.buffer();
        // 依次将header中的字段写入Buffer缓冲区
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        // 获取消息中指定的序列化器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if(serializerEnum == null){
            throw new RuntimeException("序列化器不存在");
        }
        // 先获取消息体的字节数组 bodyBytes ，然后根据这个字节数组的长度设置 buffer 中的长度字段
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        buffer.appendInt(bodyBytes.length);
        buffer.appendBytes(bodyBytes);

        return buffer;
    }
}
