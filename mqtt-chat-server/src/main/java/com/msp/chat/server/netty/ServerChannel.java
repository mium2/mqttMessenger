package com.msp.chat.server.netty;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public interface ServerChannel {
    
    Object getAttribute(Object key);
    
    void setAttribute(Object key, Object value);
    
    void setIdleTime(int idleTime);
    
    void close(boolean immediately);
    
    void write(Object value);
}
