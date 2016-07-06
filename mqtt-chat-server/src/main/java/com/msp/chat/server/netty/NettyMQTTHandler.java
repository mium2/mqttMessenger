package com.msp.chat.server.netty;

import com.msp.chat.server.worker.MqttMsgWorkerManager;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PingRespMessage;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.msp.chat.core.mqtt.proto.messages.AbstractMessage.*;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-07
 * Time: 오후 3:59
 * To change this template use File | Settings | File Templates.
 */
@Sharable
public class NettyMQTTHandler extends ChannelHandlerAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("server");
    private final Map<ChannelHandlerContext, NettyChannel> m_channelMapper = new HashMap<ChannelHandlerContext, NettyChannel>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        AbstractMessage msg = (AbstractMessage) message;
        try {
            NettyChannel channel;
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("###[NettyMQTTHandler channelRead] msg.getMessageType():"+msg.getMessageType());
            }
            if(msg.getMessageType() == PINGREQ){
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("###[NettyMQTTHandler channelRead] ping");
                }
                PingRespMessage pingResp = new PingRespMessage();
                ctx.writeAndFlush(pingResp);
//                channel = m_channelMapper.get(ctx);
//                String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
//                MqttMsgWorkerManager.getInstance().receivePing(clientID);
            }else if(msg.getMessageType() == PINGRESP){
            }else{
                synchronized(m_channelMapper) {
                    if (!m_channelMapper.containsKey(ctx)) {
                        m_channelMapper.put(ctx, new NettyChannel(ctx));
                    }
                    channel = m_channelMapper.get(ctx);
                }
                MqttMsgWorkerManager.getInstance().handleProtocolMessage(channel, msg);
            }

/*            switch (msg.getMessageType()) {
                case CONNECT:
                case SUBSCRIBE:
                case UNSUBSCRIBE:
                case GROUPSUBSCRIBE:
                case PUBLISH:
                case PUBREC:
                case PUBCOMP:
                case PUBREL:
                case DISCONNECT:
                case PUBACK:
                    NettyChannel channel;
                    synchronized(m_channelMapper) {
                        if (!m_channelMapper.containsKey(ctx)) {
                            m_channelMapper.put(ctx, new NettyChannel(ctx));
                        }
                        channel = m_channelMapper.get(ctx);
                    }
                    MqttMsgWorkerManager.getInstance().handleProtocolMessage(channel, msg);
                    break;
                //핑은 메세지 처리 메모리에 넣치 않고 바로 응답 처리 한다.
                case PINGREQ:
                    PingRespMessage pingResp = new PingRespMessage();
                    ctx.writeAndFlush(pingResp);
                    channel = m_channelMapper.get(ctx);
                    String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
                    MqttMsgWorkerManager.getInstance().receivePing(clientID);
                    break;
            }*/
        } catch (Exception ex) {
            LOGGER.error("###[NettyMQTTHandler Exception] Bad error in processing the message", ex);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)throws Exception {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[NettyMQTTHandler channelInactive]");
        }
        NettyChannel channel = m_channelMapper.get(ctx);
        if(channel!=null) {
            String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);

            if(clientID!=null) {
                boolean doubleLogin = false;
                Object obj = channel.getAttribute(Constants.DOUBLE_LOGIN);
                if(obj!=null){
                    try {
                        doubleLogin = (Boolean)obj;
                        if(LOGGER.isInfoEnabled()) {
                            LOGGER.info("###[NettyMQTTHandler channelInactive] channel.getAttribute(Constants.DOUBLE_LOGIN) : {}",channel.getAttribute(Constants.DOUBLE_LOGIN));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                // 중복로그인이 아니므로 lostConnection 비즈니스로직을 처리 하도록 호출한다.
                if(!doubleLogin) {
                    MqttMsgWorkerManager.getInstance().lostConnection(clientID);
                }
            }
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
            LOGGER.trace("###[NettyMQTTHandler exceptionCaught] Mqtt Handler Exception : {}", cause.getMessage());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[NettyMQTTHandler userEventTriggered] userEventTriggered:{}", evt.toString());
        }
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();

            if (e == IdleState.ALL_IDLE) {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("###[NettyMQTTHandler userEventTriggered] ALL_IDLE");
                }
                ctx.close();
            }
        }
    }
}
