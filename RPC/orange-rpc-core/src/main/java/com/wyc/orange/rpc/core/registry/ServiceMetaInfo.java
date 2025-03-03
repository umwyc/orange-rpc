package com.wyc.orange.rpc.core.registry;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * 服务元信息（用于注册中心有关服务）
 */
@Data
public class ServiceMetaInfo {


    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号（从 RpcConfig 中获取）
     */
    private String serviceVersion = "1.0";

    /**
     * 服务域名
     */
    private String serviceHost;

    /**
     * 服务端口号
     */
    private Integer servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup = "default";

    /**
     * 获取服务键名
     * 服务名称 + 服务版本号唯一确定一个服务
     *
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务节点键名
     * 服务名称 + 服务版本号 + 节点的访问地址唯一确定一个节点
     *
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 节点的地址
     *
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

}
