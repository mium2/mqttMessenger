package com.msp.chat.server.netty;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PingRespMessage;
import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.worker.MqttMsgWorkerManager;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static com.msp.chat.core.mqtt.proto.messages.AbstractMessage.*;
import static com.msp.chat.core.mqtt.proto.messages.AbstractMessage.PINGREQ;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class NettySslMqttHandler extends ChannelHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger("server");
    private final Map<ChannelHandlerContext, NettyChannel> m_channelMapper = new HashMap<ChannelHandlerContext, NettyChannel>();

    protected static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        //Once session is secured. send a greeting and register ther channel to the global channel

        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        System.out.println("####### Welcome to "+ InetAddress.getLocalHost().getHostName()+" secure chat service!");
                        System.out.println("#######Your session is protected by " + ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() + " cipher suite.");
                        ctx.writeAndFlush("Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
                        ctx.writeAndFlush("Your session is protected by " + ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() + " cipher suite.\n");
                        channels.add(ctx.channel());
                        System.out.println("##### @@channels size:" + channels.size());
                    }
                }
        );
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        if(message instanceof  AbstractMessage) {
            AbstractMessage msg = (AbstractMessage) message;
            try {
                System.out.println("Handler 받은 메세지 타입 :" + msg.getMessageType());
                switch (msg.getMessageType()) {
                    case CONNECT:
                        System.out.println("#######CONNECT");
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
                        synchronized (m_channelMapper) {
                            if (!m_channelMapper.containsKey(ctx)) {
                                m_channelMapper.put(ctx, new NettyChannel(ctx));
                            }
                            channel = m_channelMapper.get(ctx);
                        }
                        MqttMsgWorkerManager.getInstance().handleProtocolMessage(channel, msg);
                        break;
                    //핑은 메세지 Process 처리 메모리에 넣치 않고 바로 응답 처리 한다.
                    case PINGREQ:
                        LOGGER.debug("#################  PINGREQ");
                        PingRespMessage pingResp = new PingRespMessage();
                        ctx.writeAndFlush(pingResp);
                        channel = m_channelMapper.get(ctx);
                        String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
                        MqttMsgWorkerManager.getInstance().receivePing(clientID);
                        break;
                }
            } catch (Exception ex) {
                LOGGER.error("Bad error in processing the message", ex);
            }
        }else{
            String request = new String((byte[])message);
            System.out.println("" + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> + "+ request);
            ctx.writeAndFlush(request.getBytes());
        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx)throws Exception {
        LOGGER.debug("########### channelInactive");
        NettyChannel channel = m_channelMapper.get(ctx);
        if(channel!=null) {
            String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
            if(clientID!=null) {
                MqttMsgWorkerManager.getInstance().lostConnection(clientID);
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
            LOGGER.trace("###[exceptionCaught] Mqtt Handler Exception : {}", cause.getMessage());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if(LOGGER.isTraceEnabled()) {
            LOGGER.trace("###[IdleTimoutHandler] userEventTriggered:{}", evt.toString());
        }
        if (evt instanceof IdleStateEvent) {
            IdleState e = ((IdleStateEvent) evt).state();

            if (e == IdleState.ALL_IDLE) {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("###[userEventTriggered] ALL_IDLE");
                }
                ctx.close();
            }
        }
    }
}
