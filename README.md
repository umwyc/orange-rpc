# orange-rpc - 一个简易的个人 rpc 框架</br>
### 项目模块划分：</br>
#### common : 公共模块
#### provider : 服务提供者模块（有 rpc 调用实例）
#### consumer : 服务消费者模块（有 rpc 调用实例）
#### orange-rpc-cor : rpc 框架核心</br></br>

![image](https://wyc-orange-rpc.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-03%20170853.png)</br></br>
![image](https://wyc-orange-rpc.oss-cn-shenzhen.aliyuncs.com/present/%E5%B1%8F%E5%B9%95%E6%88%AA%E5%9B%BE%202025-03-03%20184713.png)</br></br></br></br>


### 项目概述：</br>
#### 通过 Java 反射机制与动态代理增强原有类从而实现 rpc 远程调用
#### 使用 Java 的 SPI 机制实现了更灵活地加载实现类
#### 基于工厂模式与 application.properties 配置文件实现了自定义配置（如序列化器、节点的ip端口号等等）
#### 基于 Vertx 编写高性能的服务器与客户端，并且支持 Http 协议与自定义协议
#### 使用 etcd 作为高性能的服务注册中心，提供服务注册、服务下线、服务监听、服务发现、心跳检测等功能
#### 实现了简单的客户端负载均衡，支持随机、轮询、一致性哈希三种不同的负载均衡策略
#### 在原有项目的基础上进行了一些简单的优化，比如使用使用 Map、List 做缓存</br></br></br></br>

### 最后：
项目的文档后续还将进一步完善，在项目里新增的功能也会尽快做出相关说明。

