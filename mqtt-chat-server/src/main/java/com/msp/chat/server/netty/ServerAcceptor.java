package com.msp.chat.server.netty;

import java.io.IOException;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public interface ServerAcceptor {
    
    void initialize() throws IOException;
    
    void close();
}
