package com.msp.chat.server.worker;

/**
 * Created by Y.B.H(mium2) on 16. 7. 20..
 */
public interface IAuthenticator {

    boolean checkValid(String username, String password);
}
