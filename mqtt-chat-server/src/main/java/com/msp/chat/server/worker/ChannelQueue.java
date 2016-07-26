package com.msp.chat.server.worker;

import com.msp.chat.server.bean.ConnectionDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Y.B.H(mium2) on 16. 7. 20..
 */
public class ChannelQueue {
    private final Logger LOGGER = LoggerFactory.getLogger("server");
    private ConcurrentMap<String, ConnectionDescriptor> clientIDSessionMap = new ConcurrentHashMap<String, ConnectionDescriptor>();
    private static ChannelQueue ourInstance = new ChannelQueue();

    public static ChannelQueue getInstance() {
        return ourInstance;
    }

    private ChannelQueue() {
    }

    public void putChannel(String clientID, ConnectionDescriptor connectionDescriptor){
        clientIDSessionMap.put(clientID,connectionDescriptor);
    }

    public ConnectionDescriptor getChannel(String clientID){
        return clientIDSessionMap.get(clientID);
    }

    public ConnectionDescriptor removeChannel(String clientID){
        return clientIDSessionMap.remove(clientID);
    }

    public boolean isContainsKey(String clientID){
        return clientIDSessionMap.containsKey(clientID);
    }

    public int getSize(){
        return clientIDSessionMap.size();
    }
}
