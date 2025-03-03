package com.wyc.consumer.proxy;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类型获取代理对象
     *
     * @param <T>
     * @param serviceClass 要被代理的类
     * @return T
     */
    public static <T> T getProxy(Class<T> serviceClass, boolean isHttp){
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                isHttp ? new HttpServiceProxy() : new TcpServiceProxy()
        );
    }
}
