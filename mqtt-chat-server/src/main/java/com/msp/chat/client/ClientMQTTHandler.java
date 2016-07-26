package com.msp.chat.client;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PingRespMessage;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.msp.chat.core.mqtt.proto.messages.AbstractMessage.PINGREQ;
import static com.msp.chat.core.mqtt.proto.messages.AbstractMessage.PUBACK;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
@Sharable
public class ClientMQTTHandler extends ChannelHandlerAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("server");
    private final Map<ChannelHandlerContext, NettyChannel> m_channelMapper = new HashMap<ChannelHandlerContext, NettyChannel>();
    private final String BROKERID;

    public ClientMQTTHandler(String brokerid){
        this.BROKERID = brokerid;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        AbstractMessage msg = (AbstractMessage) message;
        try {
            NettyChannel channel;
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("###[ClientMQTTHandler channelRead] msg.getMessageType():"+msg.getMessageType());
            }
            if(msg.getMessageType() == PINGREQ) {
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("###[ClientMQTTHandler channelRead]channelRead ping");
                }
                PingRespMessage pingResp = new PingRespMessage();
                ctx.writeAndFlush(pingResp);
            }else if(msg.getMessageType() == PUBACK){
                // TODO : 다른 브로커서버에 메세지 발송위임하고 전달 받은 ACK 처리방안. ACK를 받지 못하면 offmessage에 넣는게 좋을 듯함.
                LOGGER.info("###[ClientMQTTHandler channelRead] SYSTEM PUBLISH MESSAGE ACK 들어옴.");
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

        if(ctx!=null) {
            ctx.close(/*false*/);
            synchronized (m_channelMapper) {
                m_channelMapper.remove(ctx);
            }
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
