package com.mium2.messenger.util.client;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Y.B.H(mium2) on 16. 10. 4..
 */
public class MessageQueue {

    /* queue */
    private static ConcurrentLinkedQueue<Object> message = new ConcurrentLinkedQueue();

    public static void setMessage(Object obj) {

        message.offer(obj);
    }

    public static Object getMessage() {
        return  message.poll();
    }
}
