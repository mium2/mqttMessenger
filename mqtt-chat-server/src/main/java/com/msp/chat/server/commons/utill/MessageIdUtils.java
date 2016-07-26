package com.msp.chat.server.commons.utill;
/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class MessageIdUtils {
	private static final int MIN_MSG_ID = 1;		// Lowest possible MQTT message ID to use
	private static final int MAX_MSG_ID = 65535;	// Highest possible MQTT message ID to use
	private static int nextMsgId = MIN_MSG_ID - 1;			// The next available message ID to use

    public synchronized static int getNextMessageId() {    	
	    nextMsgId++;
	    if ( nextMsgId > MAX_MSG_ID ) {
	        nextMsgId = MIN_MSG_ID;
	    }
	    return nextMsgId;
    }
}
