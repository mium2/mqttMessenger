package com.msp.chat.core.mqtt.decoder;

import com.msp.chat.core.mqtt.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage.QOSType;
import com.msp.chat.core.mqtt.proto.messages.SubscribeMessage;

/**
 *
 * @author andrea
 */
class SubscribeDecoder extends DemuxDecoder {

    @Override
    void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //Common decoding part
        SubscribeMessage message = new SubscribeMessage();
        in.resetReaderIndex();
        if (!decodeCommonHeader(message, in)) {
            in.resetReaderIndex();
            return;
        }
        
        //check qos level
        if (message.getQos() != QOSType.LEAST_ONE) {
            throw new CorruptedFrameException("Received Subscribe message with QoS other than LEAST_ONE, was: " + message.getQos());
        }
            
        int start = in.readerIndex();
        //read  messageIDs
        message.setMessageID(in.readUnsignedShort());
        int readed = in.readerIndex() - start;
        while (readed < message.getRemainingLength()) {
            decodeSubscription(in, message);
            readed = in.readerIndex()- start;
        }
        
        out.add(message);
    }
    
    /**
     * Populate the message with couple of Qos, topic
     */
    private void decodeSubscription(ByteBuf in, SubscribeMessage message) throws UnsupportedEncodingException {
        String topic = ByteUtils.decodeString(in);
        byte qos = (byte)(in.readByte() & 0x03);
        message.addSubscription(new SubscribeMessage.Couple(qos, topic));
    }
    
}
