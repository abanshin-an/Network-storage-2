package ru.gb.storage.commons.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.gb.storage.commons.message.Message;

import java.io.IOException;
import java.util.List;

public class JSONDecoder extends MessageToMessageDecoder<ByteBuf> {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws IOException {
        byte[] bytes = ByteBufUtil.getBytes(msg);
        Message message = OBJECT_MAPPER.readValue(bytes, Message.class);
        out.add(message);
    }

}