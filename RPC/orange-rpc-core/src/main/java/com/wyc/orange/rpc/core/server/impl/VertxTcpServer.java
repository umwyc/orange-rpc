package com.wyc.orange.rpc.core.server.impl;

import com.wyc.orange.rpc.core.server.handler.TcpServerHandler;
import com.wyc.orange.rpc.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

/**
 * 使用TCP协议的服务器实现类
 */
public class VertxTcpServer implements Server {
    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP服务端
        NetServer netServer = vertx.createNetServer();

        // 绑定请求处理器
        netServer.connectHandler(new TcpServerHandler());

        // 启动服务器并监听服务器端口
        netServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is listening on port " + port);
            }else{
                System.out.println("Failed to listen on port " + port);
            }
        });
    }
}
