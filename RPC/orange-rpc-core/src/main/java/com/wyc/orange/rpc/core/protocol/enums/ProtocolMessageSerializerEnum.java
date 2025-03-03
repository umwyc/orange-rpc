package com.wyc.orange.rpc.core.protocol.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 协议使用的序列化器
 */
@Getter
public enum ProtocolMessageSerializerEnum {

    JDK(0, "jdk"),
    JSON(1, "json"),
    KRYO(2, "kryo"),
    HESSIAN(3, "hessian");

    private int key;

    private String value;

    ProtocolMessageSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 获取值的列表
     *
     * @return
     */
    public static List<String> getValues(){
        return Arrays.stream(ProtocolMessageSerializerEnum.values())
                .map(item -> item.value)
                .collect(Collectors.toList());
    }

    /**
     * 根据键来获取枚举
     *
     * @param key
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByKey(int key){
        for(ProtocolMessageSerializerEnum item : ProtocolMessageSerializerEnum.values()){
            if(item.key == key){
                return item;
            }
        }
        return null;
    }

    /**
     * 根据值来获取枚举
     *
     * @param value
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByValue(String value){
        for(ProtocolMessageSerializerEnum item : ProtocolMessageSerializerEnum.values()){
            if(item.value.equals(value)){
                return item;
            }
        }
        return null;
    }
}
