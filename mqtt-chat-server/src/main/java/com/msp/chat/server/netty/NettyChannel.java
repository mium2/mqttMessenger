package com.msp.chat.server.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import com.msp.chat.core.mqtt.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오후 2:23
 */
public class NettyChannel implements ServerChannel {
    
    private ChannelHandlerContext m_channel;
    
    private Map<Object, AttributeKey<Object>> m_attributesKeys = new HashMap<Object, AttributeKey<Object>>();
    
//    private static final AttributeKey<Object> ATTR_KEY_KEEPALIVE = new AttributeKey<Object>(Constants.KEEP_ALIVE);
    private static final AttributeKey<Object> ATTR_KEY_KEEPALIVE = AttributeKey.valueOf(NettyChannel.class, Constants.KEEP_ALIVE);
//    private static final AttributeKey<Object> ATTR_KEY_CLEANSESSION = new AttributeKey<Object>(Constants.CLEAN_SESSION);
    private static final AttributeKey<Object> ATTR_KEY_CLEANSESSION = AttributeKey.valueOf(NettyChannel.class, Constants.CLEAN_SESSION);
//    private static final AttributeKey<Object> ATTR_KEY_CLIENTID = new AttributeKey<Object>(Constants.ATTR_CLIENTID);
    private static final AttributeKey<Object> ATTR_KEY_CLIENTID = AttributeKey.valueOf(NettyChannel.class, Constants.ATTR_CLIENTID);
    private static final AttributeKey<Object> ATTR_KEY_DOUBLE_LOGIN = AttributeKey.valueOf(NettyChannel.class, Constants.DOUBLE_LOGIN);

    NettyChannel(ChannelHandlerContext ctx) {
        m_channel = ctx;
        m_attributesKeys.put(Constants.KEEP_ALIVE, ATTR_KEY_KEEPALIVE);
        m_attributesKeys.put(Constants.CLEAN_SESSION, ATTR_KEY_CLEANSESSION);
        m_attributesKeys.put(Constants.ATTR_CLIENTID, ATTR_KEY_CLIENTID);
        m_attributesKeys.put(Constants.DOUBLE_LOGIN, ATTR_KEY_DOUBLE_LOGIN);
    }

    public Object getAttribute(Object key) {
        Attribute<Object> attr = m_channel.attr(mapKey(key));
        return attr.get();
    }

    public void setAttribute(Object key, Object value) {
        Attribute<Object> attr = m_channel.attr(mapKey(key));
        attr.set(value);
    }
    
    private synchronized AttributeKey<Object> mapKey(Object key) {
        if (!m_attributesKeys.containsKey(key)) {
            throw new IllegalArgumentException("mapKey can't find a matching AttributeKey for " + key);
        }
        return m_attributesKeys.get(key);
    }

    public void setIdleTime(int idleTime) {
        if (m_channel.pipeline().names().contains("idleStateHandler")) {
            m_channel.pipeline().remove("idleStateHandler");
        }
        if (m_channel.pipeline().names().contains("idleEventHandler")) {
            m_channel.pipeline().remove("idleEventHandler");
        }
        m_channel.pipeline().addFirst("idleStateHandler", new IdleStateHandler(0, 0, idleTime));
    }

    public void close(boolean immediately) {
        if(!immediately){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        m_channel.close();
    }

    public void write(Object value) {
        m_channel.writeAndFlush(value);
    }
    
}
