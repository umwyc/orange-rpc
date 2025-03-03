package com.wyc.orange.rpc.core.constants;

public interface ProtocolConstant {

    /**
     * 消息头长度
     */
    int MESSAGE_HEADER_LENGTH = 17;

    /**
     * 魔数
     */
    byte PROTOCOL_MAGIC = 0x1;

    /**
     * 协议版本（从 RpcConfig 中获取）
     */
    byte PROTOCOL_VERSION = 0x1;
}
