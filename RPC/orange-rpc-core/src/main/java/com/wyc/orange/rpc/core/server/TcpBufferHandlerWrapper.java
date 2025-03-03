package com.wyc.orange.rpc.core.server;

import com.wyc.orange.rpc.core.constants.ProtocolConstant;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 装饰者模式（使用 recordParser 对原有的 buffer 处理能力进行增强）
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> handler) {
        recordParser = initRecordParser(handler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    /**
     * 使用装饰者模式对原有的实例进行增强
     * @param handler
     * @return
     */
    private RecordParser initRecordParser(Handler<Buffer> handler) {
        // 构造解析器（初始时的大小恰好为消息头的长度）
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        // 构造处理器（当解析器读到一个完整的记录时处理器会被触发）
        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if(size == -1){
                    // 读取消息体的长度
                    size = buffer.getInt(13);
                    parser.fixedSizeMode(size);
                    // 读取消息头
                    resultBuffer.appendBuffer(buffer);
                }else{
                    // 读取消息体
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整的Buffer，执行处理
                    handler.handle(resultBuffer);
                    // 重置一轮
                    parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                    size = -1;
                    resultBuffer = Buffer.buffer();
                }
            }
        });

        return parser;
    }
}
