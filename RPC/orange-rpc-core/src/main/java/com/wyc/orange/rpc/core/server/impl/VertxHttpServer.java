package com.wyc.orange.rpc.core.server.impl;

import com.wyc.orange.rpc.core.server.handler.HttpServerHandler;
import com.wyc.orange.rpc.core.server.Server;
import io.vertx.core.Vertx;

/**
 * 使用HTTP协议的服务器实现类
 */
public class VertxHttpServer implements Server {
    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建Http服务器
        io.vertx.core.http.HttpServer httpServer = vertx.createHttpServer();

        // 绑定请求处理器
        httpServer.requestHandler(new HttpServerHandler());

        // 服务器监听指定端口并启动服务器
        httpServer.listen(port, result->{
           if(result.succeeded()) {
               System.out.println("Server is listening on port " + port);
           }else {
               System.err.println("Failed to listen on port " + port);
           }
        });
    }
}
