package com.msp.chat.server.netty;

/**
 *
 * @author andrea
 */
public interface ServerChannel {
    
    Object getAttribute(Object key);
    
    void setAttribute(Object key, Object value);
    
    void setIdleTime(int idleTime);
    
    void close(boolean immediately);
    
    void write(Object value);
}
