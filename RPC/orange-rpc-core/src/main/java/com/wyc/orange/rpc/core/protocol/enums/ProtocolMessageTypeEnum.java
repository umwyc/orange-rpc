package com.wyc.orange.rpc.core.protocol.enums;

import lombok.Getter;

/**
 * 协议消息类型
 */
@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    public static ProtocolMessageTypeEnum getEnumByKey(int key){
        for (ProtocolMessageTypeEnum value : ProtocolMessageTypeEnum.values()) {
            if(value.key == key){
                return value;
            }
        }
        return null;
    }
}
