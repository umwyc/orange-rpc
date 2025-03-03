package com.wyc.orange.rpc.core.example;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 * 客户端实例代码
 */
public class VertxTcpClientExample {

    public static void main(String[] args) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP客户端
        NetClient netClient = vertx.createNetClient();

        // 使用客户端连接至服务端
        netClient.connect(8888, "localhost", result -> {
            if (result.succeeded()) {
                System.out.println("客户端启动成功");
                NetSocket socket = result.result();
                // 使用客户端发送请求
                socket.write(Buffer.buffer("Hello Server"));
                socket.handler(response -> {
                    byte[] responseBytes = response.getBytes();
                    System.out.println("客户端接收响应:" + new String(responseBytes));
                });

            }else{
                System.out.println("客户端启动失败");
            }
        });

    }
}
