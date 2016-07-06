package com.mium2.push.client;

import com.google.gson.JsonObject;
import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.internal.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */

public class SendMain {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private List<String> pnsids = new ArrayList<String>();
    public final static int startPos = 0;
    public final static int endPos = 1;

    public SendMain(){
        pnsids.add("A_TOPIC");
    }
    
    public static void main(String[] args)  throws IOException, InterruptedException {

        SendMain clientMain = new SendMain();

        /*****************************************************************************
         *  Publish  Connect 테스트
         *****************************************************************************/

        /*****************************************************************************
         *  메세지 전송 방법 :1 구독/토픽을 이용한 토픽을 구독한 모든 사용자 발송
         *****************************************************************************/
        MqttClient publishClient = clientMain.newConnect("SENDER01", "A_TOPIC");

        Thread.sleep(1000);
        clientMain.testSend(publishClient);

    }



    private MqttClient newConnect(String clientID, String TOPIC){
        MqttClient mConnection = null;
        try {
            String BROKER_URL = "tcp://211.241.199.139:1883";
            int MQTT_KEEP_ALIVE = 120;

            mConnection = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
            mConnection.setCallback(new MessengerCallback());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setConnectionTimeout(10);
            mqttConnectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE);
            mConnection.connect(mqttConnectOptions);
//            mConnection.subscribe(TOPIC, 1);
            System.out.println("########### 접속 성공: 접속아이디:" + clientID + "   구독정보:" + TOPIC);
        } catch (Exception e) {
            if (e instanceof MqttException) {
                //서버의 ConnectAck 에러번호가 6일때는 다른 브로커서버로 접속하라고 구현
                if (((MqttException) e).getReasonCode() == 6) {
                    logger.error("접속할 다른 서버를 알아내는 로직 구현해야함");
                }
            }else {
                logger.error("#####에러:" + e.getMessage());
            }
        }
        return mConnection;
    }

    private void testSend(MqttClient mqttClient){
//        for (int i = 0; i < pnsids.size(); i++) {
        for (int i = 0; i < 1; i++) {
			/* PNS 전송 부분 */
            String msgPsid = pnsids.get(0);
            String appId = "com.ingni.push1";
            String msgMessage = "This is Test message~~!";
            String msgPublic = "N";
            String msgUniqueKey = ""+i;
            String SEQNO = ""+i;
            String CUID = "TEST01";
            String DEVICESEQ = "1";
            String PNSINFOSEQ = "1";
            String DEVICEID = "DEV_01";
            String SERVICECODE = "DEV_01";
            JsonObject jsoWhole = new JsonObject();
            JsonObject jsoHeader = new JsonObject();
            JsonObject jsoBody = new JsonObject();

            jsoHeader.addProperty("ACTION", "SENDMSG");
            jsoBody.addProperty("MESSAGE", msgMessage);
            jsoBody.addProperty("PSID", msgPsid);
            jsoBody.addProperty("APPID", appId);
            jsoBody.addProperty("MESSAGEID", msgUniqueKey);
            jsoBody.addProperty("PUBLIC", msgPublic);

            jsoBody.addProperty("SEQNO", SEQNO);
            jsoBody.addProperty("CUID", CUID);
            jsoBody.addProperty("DEVICESEQ", DEVICESEQ);
            jsoBody.addProperty("PNSINFOSEQ", PNSINFOSEQ);
            jsoBody.addProperty("DEVICEID", DEVICEID);
            jsoBody.addProperty("SERVICECODE", SERVICECODE);
            jsoBody.addProperty("PNSID", "UPNC");
            ////////////////////////////////////////////////////////////////////////////////////////////////////////
            jsoWhole.add("HEADER", jsoHeader);
            jsoWhole.add("BODY", jsoBody);

            System.out.println("보내는 메세지:"+msgMessage.toString());
            long statTime = System.currentTimeMillis();
            try {
                for(int j=startPos; j<endPos; j++) {
//                    mqttClient.publish("A_TOPIC", msgMessage.toString().getBytes(), 1, false);
                    mqttClient.publish("A_TOPIC", msgMessage.toString().getBytes(), 1, false, j);
                }
//                mqttClient.publish("A_TOPIC", msgMessage.toString().getBytes(), 1, false, MsgIDManager.getInstance().getNextMessgeID());
            } catch (Exception e) {
                e.printStackTrace();
            }
            long endTime = System.currentTimeMillis();

            System.out.println("경과시간:"+(endTime-statTime));
        }
    }

    public static void testByte(){
        int testInt = 16;
//        for(int testInt=0; testInt<20; testInt++) {
            System.out.println("===== ByteOrder.LITTLE_ENDIAN  ===");
            byte[] bytes = intTobyte(testInt, ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < bytes.length; i++) {
                System.out.printf("[%02X]", bytes[i]);
            }
            System.out.println();
            System.out.println(byteToInt(bytes, ByteOrder.LITTLE_ENDIAN));

/*            System.out.println("===== ByteOrder.BIG_ENDIAN  ===");
            bytes = intTobyte(testInt, ByteOrder.BIG_ENDIAN);

            for (int i = 0; i < bytes.length; i++) {
                System.out.printf("[%02X]", bytes[i]);
            }
            System.out.println();
            System.out.println(byteToInt(bytes, ByteOrder.BIG_ENDIAN));*/
        }
//    }

    /**
     * int형을 byte배열로 바꿈<br>
     * @param integer
     * @param order
     * @return
     */
    public static byte[] intTobyte(int integer, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8); //1byte => 8bit
        buff.order(order);

        // 인수로 넘어온 integer을 putInt로설정
        buff.putInt(integer);

        System.out.println("intTobyte : " + buff);
        return buff.array();
    }

    /**
     * byte배열을 int형로 바꿈<br>
     * @param bytes
     * @param order
     * @return
     */
    public static int byteToInt(byte[] bytes, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
        buff.order(order);

        // buff사이즈는 4인 상태임
        // bytes를 put하면 position과 limit는 같은 위치가 됨.
        buff.put(bytes);
        // flip()가 실행 되면 position은 0에 위치 하게 됨.
        buff.flip();

        System.out.println("byteToInt : " + buff);

        return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
    }

}




