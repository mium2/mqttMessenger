package com.mium2.push.client;

import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.internal.MemoryPersistence;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-01-21
 * Time: 오후 1:32
 * To change this template use File | Settings | File Templates.
 */
public class SubscribeMain {
//    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    public SubscribeMain(){
    }

    public static void main(String[] args)  throws IOException, InterruptedException {

        SubscribeMain subscribeMain = new SubscribeMain();

        MqttClient mqttClient = subscribeMain.newConnect("SUBSCRIBE_0","A_TOPIC");
    }

    private MqttClient newConnect(String clientID, String TOPIC){
        MqttClient mConnection = null;
        try {
            String BROKER_URL = "tcp://localhost:1883";
            int MQTT_KEEP_ALIVE = 60;

            //스토리지 서비스 MqttDefaultFilePersistence (파일스토리지
            mConnection = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
            mConnection.setCallback(new MessengerCallback());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setConnectionTimeout(10);
            mqttConnectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE);
            mConnection.connect(mqttConnectOptions);
            mConnection.subscribe(TOPIC, 1);
            System.out.println("########### 접속 성공: 접속아이디:" + clientID + "   구독정보:" + TOPIC);
        } catch (Exception e) {
            if (e instanceof MqttException) {
                //서버의 ConnectAck 에러번호가 6일때는 다른 브로커서버로 접속하라고 구현
                if (((MqttException) e).getReasonCode() == 6) {
                    System.out.println("접속할 다른 서버를 알아내는 로직 구현해야함");
                }
            }else {
                System.out.println("#####에러:" + e.getMessage());
            }
        }
        return mConnection;
    }
}
