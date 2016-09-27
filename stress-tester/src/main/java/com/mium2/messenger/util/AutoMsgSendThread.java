package com.mium2.messenger.util;

import kr.msp.upns.client.mqttv3.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 9. 27..
 */
public class AutoMsgSendThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private StressTester stressTester;

    public AutoMsgSendThread(String threadName, StressTester _stressTester){
        super.setName(threadName);
        this.stressTester = _stressTester;
    }

    @Override
    public void run() {
        while(true) {
            String randomRoomIdSenderId = stressTester.getRandomRoomIDSenderIdMap();
            String[] randomRoomIdSenderIdArr = randomRoomIdSenderId.split("\\|");
            String roomID = randomRoomIdSenderIdArr[0];
            String senderID = randomRoomIdSenderIdArr[1];


            logger.debug("## roomID:"+roomID+"     senderID:"+senderID);
            MqttClient mqttClient = stressTester.getMqttClient(senderID);
            if(mqttClient.isConnected()){
                try {
                    String sendMsg="하하하 메신저 성능 테스트 입니다.";
                    mqttClient.publish(roomID, sendMsg.toString().getBytes(), 1, false, MsgIDManager.getInstance().getNextMessgeID());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000*60);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
