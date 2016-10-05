package com.mium2.messenger.util.client;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PubAckMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
@Sharable
public class ClientMQTTHandler extends ChannelHandlerAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("com.mium2.messenger.util");

    public ClientMQTTHandler(){}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        AbstractMessage msg = (AbstractMessage) message;
        try {
            switch ( msg.getMessageType() ) {
                case AbstractMessage.CONNACK:
                    break;
                case AbstractMessage.SUBACK:
                    break;
                case AbstractMessage.PUBLISH:
                    PublishMessage publish = (PublishMessage)msg;
                    MessageQueue.setMessage(new MessageInfo(ctx,publish));
                    break;
                case AbstractMessage.PUBACK:
                case AbstractMessage.PUBREC:
                case AbstractMessage.PUBCOMP:
                case AbstractMessage.PUBREL:
                case AbstractMessage.DISCONNECT:
                case AbstractMessage.PINGREQ:
                    break;
                case AbstractMessage.PINGRESP:
                    break;
            }
        } catch (Exception ex) {
            LOGGER.error("###[ClientMQTTHandler channelRead] Bad error in processing the message", ex);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)throws Exception {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[ClientMQTTHandler channelInactive]");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[ClientMQTTHandler exceptionCaught] Mqtt Handler Exception : {}", cause.getMessage());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[ClientMQTTHandler userEventTriggered] userEventTriggered:{}", evt.toString());
        }
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();

            if (e == IdleState.ALL_IDLE) {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("###[ClientMQTTHandler userEventTriggered] ALL_IDLE");
                }
                ctx.close();
            }
        }
    }
}
