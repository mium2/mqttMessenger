package com.msp.chat.client;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 5. 4..
 */
public class SubscribeMain {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)  throws IOException, InterruptedException {
        /*****************************************************************************
         *  클라이언트 쓰레드로 여러개 구동 시키기 테스트
         *****************************************************************************/

        String clientID = "SUBSCRIBE_";
        String[] topic = {"A_TOPIC"};

        final Client client = new Client("localhost", 1883);
        try {
            for(int i=0; i<1; i++) {
                client.connect(false, clientID+i);  //세션 클린 하지 않는다.
                Thread.sleep(100);
                client.subscribe(topic, new IPublishCallback() {
                    public void published(String topic, String revMsg) {
                        System.out.println("################ 서버에서 들어온 구독 메세지:" + revMsg);
                    }
                });
            }
        }catch (InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}
