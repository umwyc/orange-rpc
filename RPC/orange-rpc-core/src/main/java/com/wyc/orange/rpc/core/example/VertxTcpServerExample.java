package com.wyc.orange.rpc.core.example;

import com.wyc.orange.rpc.core.server.Server;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;

/**
 * 服务端实例代码
 */
public class VertxTcpServerExample implements Server {

    private byte[] handleRequest(byte[] requestBytes){
        return "Hello Client".getBytes();
    }

    @Override
    public void doStart(int port) {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP服务端
        NetServer netServer = vertx.createNetServer();

        // 处理连接
        netServer.connectHandler(socket -> {
            // 处理请求
            socket.handler(request -> {
                // 接收请求
                byte[] requestBytes = request.getBytes();
                System.out.println("服务器接收请求:" + new String(requestBytes));
                // 得到响应
                byte[] responseBytes = handleRequest(requestBytes);
                // 发送响应
                socket.write(Buffer.buffer(responseBytes));
            });
        });

        // 启动服务器并监听端口
        netServer.listen(port, "localhost", result -> {
           if(result.succeeded()){
               System.out.println("服务器启动成功，正在监听端口:" + port);
           }else{
               System.out.println("服务器启动失败");
           }
        });
    }

    public static void main(String[] args) {
        VertxTcpServerExample vertxTcpServerExample = new VertxTcpServerExample();
        vertxTcpServerExample.doStart(8888);
    }
}
