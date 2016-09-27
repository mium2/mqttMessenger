package com.msp.messenger.service.mqttclient;

import kr.msp.upns.client.mqttv3.*;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 19..
 */
public class BrokerCallback implements MqttCallback {


    public BrokerCallback() {
    }

    @Override
    public void connectionLost(Throwable cause) {
        //재접속 시도 로직 구현
        System.out.println("###### 이유:"+cause.toString());
        System.out.println("##########서버 연결 끊김 (1.클라이언트 네트웍이 끊겼을경우, 2.서버가 죽었을경우 ");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        System.out.println("###UPNS 받은 메세지:"+mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        try {

            if(iMqttDeliveryToken!=null && iMqttDeliveryToken.getMessage()!=null){
                System.out.println("###########토픽 정상적으로 전달됨:"+iMqttDeliveryToken.getMessage());
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
