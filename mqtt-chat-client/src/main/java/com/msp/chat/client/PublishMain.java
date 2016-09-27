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
public class PublishMain {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);
    private static boolean received;

    public static void main(String[] args)  throws IOException, InterruptedException {
        /*****************************************************************************
         *  클라이언트 쓰레드로 여러개 구동 시키기 테스트
         *****************************************************************************/

        String clientID = "TEST01";
//        Client client = new Client("211.241.199.92", 1883);
        Client client = new Client("localhost", 8443);
        try {
            for(int i=0; i<1; i++) {
                client.connect(false, clientID);  //세션 클린 하지 않는다.
                Thread.sleep(1000);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }

        /*****************************************************************************
         *  메세지 전송 방법 :1 구독/토픽을 이용한 토픽을 구독한 모든 사용자 발송
         *****************************************************************************/
        String topic = "850619141";
        String payload = "안녕하세요. 테스트 메세지입니다.";
        for(int i=0; i<10; i++) {
            payload = payload+i;
            client.publish(topic, payload.getBytes(), AbstractMessage.QOSType.MOST_ONE, false);
        }
        System.out.println("###### 방법1:메세지 발송");
    }
}
