package com.msp.chat.server.worker;

/**
 * Created by Y.B.H(mium2) on 14. 5. 7..
 */
public interface IAuthenticator {

    boolean checkValid(String username, String password);
}
