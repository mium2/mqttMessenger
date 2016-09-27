package com.mium2.messenger.util;

import kr.msp.upns.client.mqttv3.MqttCallback;
import kr.msp.upns.client.mqttv3.MqttDeliveryToken;
import kr.msp.upns.client.mqttv3.MqttMessage;
import kr.msp.upns.client.mqttv3.MqttTopic;
import kr.msp.upns.client.mqttv3.internal.wire.MqttWireMessage;

import java.util.Arrays;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 22..
 */
public class MessengerCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void connectionLost(Throwable cause, MqttWireMessage mqttWireMessage) {

    }

    @Override
    public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
        if(message.getPayload().length>10){
            int lastPosition = 0;
            byte[] chkSysMsgBytes = new byte[10];
            for(int i=0; i<10; i++){
                chkSysMsgBytes[i]=message.getPayload()[lastPosition];
                lastPosition++;
            }
            if(Arrays.equals(chkSysMsgBytes, Constants.SYS_MSG_SENT_COMPLETE.getBytes())){

            }else if(Arrays.equals(chkSysMsgBytes,Constants.SYS_RES_MSG_SENT_INFO.getBytes())){

            }else if(Arrays.equals(chkSysMsgBytes,Constants.SYS_RES_MSG_FILE.getBytes())){

            }

        }
        String revMsg = new String(message.getPayload(),"utf-8");
        System.out.println("\r## [받은 메세지] : " + revMsg);
        System.out.print("보낼 메세지:");
    }

    @Override
    public void deliveryComplete(MqttDeliveryToken token) {

    }
}
