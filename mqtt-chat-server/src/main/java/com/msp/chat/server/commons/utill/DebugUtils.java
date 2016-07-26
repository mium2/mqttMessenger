package com.msp.chat.server.commons.utill;

import java.nio.ByteBuffer;
/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class DebugUtils {
    public static String  payload2Str(ByteBuffer content) {
        byte[] b = new byte[content.remaining()];
        content.mark();
        content.get(b);
        content.reset();
        try {
            return new String(b,"utf-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return new String(b);
    } 
}
