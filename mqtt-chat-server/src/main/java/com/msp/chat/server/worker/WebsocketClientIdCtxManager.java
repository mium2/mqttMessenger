package com.msp.chat.server.worker;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Y.B.H(mium2) on 16. 7. 20..
 */
public class WebsocketClientIdCtxManager {

    private final Logger LOGGER = LoggerFactory.getLogger("server");
    private ConcurrentMap<String, ChannelHandlerContext> clientIDCtxMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
    private static WebsocketClientIdCtxManager instance = new WebsocketClientIdCtxManager();

    public static WebsocketClientIdCtxManager getInstance() {
        return instance;
    }

    private WebsocketClientIdCtxManager() {
    }

    public void putChannel(String clientID, ChannelHandlerContext ctx){
        clientIDCtxMap.put(clientID,ctx);
    }

    public ChannelHandlerContext getChannel(String clientID){
        return clientIDCtxMap.get(clientID);
    }

    public ChannelHandlerContext removeChannel(String clientID){
        return clientIDCtxMap.remove(clientID);
    }

    public boolean isContainsKey(String clientID){
        return clientIDCtxMap.containsKey(clientID);
    }

    public int getSize(){
        return clientIDCtxMap.size();
    }
}
