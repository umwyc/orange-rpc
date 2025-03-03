package com.wyc.orange.rpc.core.protocol.enums;

import lombok.Getter;

/**
 * 协议消息状态
 */
@Getter
public enum ProtocolMessageStatusEnum {

    OK("ok", 20),
    BAD_REQUEST("badRequest", 40),
    BAD_RESPONSE("badResponse", 50);

    private final String text;

    private final int value;

    ProtocolMessageStatusEnum(String text, Integer value){
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value){
        for (ProtocolMessageStatusEnum protocolMessageStatusEnum : ProtocolMessageStatusEnum.values()) {
            if(protocolMessageStatusEnum.getValue() == value){
                return protocolMessageStatusEnum;
            }
        }
        return null;
    }
}
