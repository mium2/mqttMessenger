package com.msp.chat.server.netty;

import java.io.IOException;

/**
 *
 * @author andrea
 */
public interface ServerAcceptor {
    
    void initialize() throws IOException;
    
    void close();
}
