package com.msp.chat.core.mqtt.encoder;

import com.msp.chat.core.mqtt.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PubAckMessage;

/**
 *
 * @author andrea
 */
class PubAckEncoder extends DemuxEncoder<PubAckMessage> {

    @Override
    protected void encode(ChannelHandlerContext chc, PubAckMessage msg, ByteBuf out) {
        ByteBuf buff = chc.alloc().buffer(6);
        try {
            buff.writeByte(AbstractMessage.PUBACK << 4);
            buff.writeBytes(ByteUtils.encodeRemainingLength(4));
            buff.writeShort(msg.getMessageID());
            buff.writeShort(msg.getSubscribeCnt());
            out.writeBytes(buff);
        } finally {
            buff.release();
        }
    }
    
}
