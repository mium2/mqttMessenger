package com.msp.chat.server.worker;

import com.msp.chat.server.netty.NettyChannel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 7. 26..
 */
public class WebsocketCtxServerHandleManager {

    private final Logger LOGGER = LoggerFactory.getLogger("server");
    private final Map<ChannelHandlerContext, NettyChannel> webSocketchannelMapper = new HashMap<ChannelHandlerContext, NettyChannel>();
    private static WebsocketCtxServerHandleManager instance = new WebsocketCtxServerHandleManager();

    public static WebsocketCtxServerHandleManager getInstance() {
        return instance;
    }

    private WebsocketCtxServerHandleManager() {
    }

    public synchronized void putNettyChannel(ChannelHandlerContext ctx){
        if (!webSocketchannelMapper.containsKey(ctx)) {
            webSocketchannelMapper.put(ctx, new NettyChannel(ctx));
        }
    }

    public synchronized NettyChannel getNettyChannel(ChannelHandlerContext ctx){
        return webSocketchannelMapper.get(ctx);
    }

    public synchronized NettyChannel removeChannel(ChannelHandlerContext ctx){
        return webSocketchannelMapper.remove(ctx);
    }

    public boolean isContainsKey(ChannelHandlerContext ctx){
        return webSocketchannelMapper.containsKey(ctx);
    }

    public int getSize(){
        return webSocketchannelMapper.size();
    }
}
