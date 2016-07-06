package com.msp.chat.core.mqtt.encoder;

import com.msp.chat.core.mqtt.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.ConnAckMessage;

/**
 *
 * @author andrea
 */
class ConnAckEncoder extends DemuxEncoder<ConnAckMessage>{

    @Override
    protected void encode(ChannelHandlerContext chc, ConnAckMessage message, ByteBuf out) {
        out.writeByte(AbstractMessage.CONNACK << 4);
        out.writeBytes(ByteUtils.encodeRemainingLength(2));
        out.writeByte(0);
        out.writeByte(message.getReturnCode());
    }
    
}
