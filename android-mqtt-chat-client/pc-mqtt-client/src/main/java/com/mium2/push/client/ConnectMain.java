package com.mium2.push.client;

import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.internal.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-01-21
 * Time: 오후 1:31
 * To change this template use File | Settings | File Templates.
 */
public class ConnectMain {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static List<String> pnsids = new ArrayList<String>();

    public ConnectMain(){
        pnsids.add("3f41e9b8dcb74ae39f9deb89007d51412fa963cb");
    }

    public static void main(String[] args)  throws IOException, InterruptedException {
        ConnectMain connectMain = new ConnectMain();
        MqttClient mqttClient = connectMain.newConnect(pnsids.get(0),pnsids.get(0));
//        Thread.sleep(10000);
        try {
            System.out.println("###### 구독신청 :"+pnsids.get(0));
            mqttClient.subscribe(pnsids.get(0),1);
//            Thread.sleep(5000);
//            System.out.println("###### 구독해제 :"+pnsids.get(0));
//            mqttClient.unsubscribe(pnsids.get(0));
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Thread.sleep(3000);
        mqttClient = connectMain.newConnect(pnsids.get(0),pnsids.get(0));
    }

    private MqttClient newConnect(String clientID, String TOPIC){
        MqttClient mConnection = null;
        try {
            String BROKER_URL = "tcp://211.241.199.139:1883";
            int MQTT_KEEP_ALIVE = 60;

            mConnection = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
            mConnection.setCallback(new MessengerCallback());
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setConnectionTimeout(10);
            mqttConnectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE);
            mConnection.connect(mqttConnectOptions);
//            mConnection.subscribe(TOPIC, 1);
            System.out.println("########### 접속 성공: 접속아이디:" + TOPIC + "   구독정보:" + TOPIC);
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

}
