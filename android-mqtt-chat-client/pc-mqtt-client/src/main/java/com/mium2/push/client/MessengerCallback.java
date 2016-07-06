package com.mium2.push.client;

import com.mium2.push.client.commons.Constants;
import kr.msp.upns.client.mqttv3.*;
import kr.msp.upns.client.mqttv3.internal.wire.MqttPubAck;
import kr.msp.upns.client.mqttv3.internal.wire.MqttPublish;
import kr.msp.upns.client.mqttv3.internal.wire.MqttWireMessage;

import java.io.FileOutputStream;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: yoo byung hee
 * Date: 14. 11. 24
 * Time: 오후 4:51
 */
public class MessengerCallback implements MqttCallback {
    private ChatMain chatMain = null;

    public MessengerCallback() {
    }

    public MessengerCallback(ChatMain _chatMain) {
        this.chatMain = _chatMain;
    }

    @Override
    public void connectionLost(Throwable cause) {
        //재접속 시도 로직 구현
        System.out.println("###### 이유:"+cause.toString());
        System.out.println("##########서버 연결 끊김 (1.클라이언트 네트웍이 끊겼을경우, 2.서버가 죽었을경우 ");
        System.exit(-1);

    }

    @Override
    public void connectionLost(Throwable cause, MqttWireMessage mqttWireMessage) {
        try {
            if(mqttWireMessage.getType()==MqttWireMessage.MESSAGE_TYPE_PUBLISH){
                MqttPublish mqttPublish = (MqttPublish)mqttWireMessage;
                String revMsg = new String(mqttPublish.getPayload(), "UTF-8");
                System.out.println("## [받은 메세지] : " + "  messageID:" + mqttPublish.getMessageId());
            }else if(mqttWireMessage.getType()==MqttWireMessage.MESSAGE_TYPE_PUBACK){
                MqttPubAck mqttPubAck = (MqttPubAck)mqttWireMessage;
                System.out.println("## 메세지 ACK :" + mqttPubAck.getMessageId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            if(Arrays.equals(chkSysMsgBytes,Constants.SYS_MSG_SENT_COMPLETE.getBytes())){

            }else if(Arrays.equals(chkSysMsgBytes,Constants.SYS_RES_MSG_SENT_INFO.getBytes())){

            }else if(Arrays.equals(chkSysMsgBytes,Constants.SYS_RES_MSG_FILE.getBytes())){
                // 확장자 length 가져오기
                byte[] fileExtentionLenBytes = new byte[4];
                for(int i=0; i<4; i++){
                    fileExtentionLenBytes[i]=message.getPayload()[lastPosition];
                    lastPosition++;
                }
                int fileExtentionLen = byteToInt(fileExtentionLenBytes);
                // 확장자 가져오기
                byte[] extentionBytes = new byte[fileExtentionLen];
                for(int i=0; i<fileExtentionLen; i++){
                    extentionBytes[i] = message.getPayload()[lastPosition];
                    lastPosition++;
                }

                // 파일명 length 가져오기
                byte[] fileNameLenBytes = new byte[4];
                for(int i=0; i<4; i++){
                    fileNameLenBytes[i]=message.getPayload()[lastPosition];
                    lastPosition++;
                }
                int fileNameLen = byteToInt(fileNameLenBytes);
                // 파일명 가져오기
                byte[] fileNameBytes = new byte[fileNameLen];
                for(int i=0; i<fileNameLen; i++){
                    fileNameBytes[i] = message.getPayload()[lastPosition];
                    lastPosition++;
                }
                String fileName = new String(fileNameBytes,"utf-8");
                if(message.getPayload().length>lastPosition) {
                    int thumImgLength = message.getPayload().length-lastPosition;
                    byte[] thumbImgBytes = new byte[thumImgLength];
                    for(int i=0; i<thumImgLength; i++){
                        thumbImgBytes[i] = message.getPayload()[lastPosition];
                        lastPosition++;
                    }
                    //썸네일이미지 저장하기
                    String orgFileFullSrc = "/Users/mium2/project/java/MqttMessenger/temp_file/client/" + fileName;
                    FileOutputStream fot = new FileOutputStream(orgFileFullSrc);
                    fot.write(thumbImgBytes);
                    fot.close();
                }
                System.out.println("\r### [시스템 받은 메세지] : " +fileName);
                return;
            }

        }
        String revMsg = new String(message.getPayload(),"utf-8");
        System.out.println("\r## [받은 메세지] : " + revMsg);
        System.out.print("보낼 메세지:");

    }

    @Override
    public void deliveryComplete(MqttDeliveryToken token) {
        //We do not need this because we do not publish
//        try {
//            MqttDeliveryTokenImpl mqttDeliveryToken = (MqttDeliveryTokenImpl)token;
//            System.out.println("## PUBLISH ACK 도착:"+mqttDeliveryToken.getMessageId());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private int byteToInt(byte[] src){
        int s1 = src[0] & 0xFF;
        int s2 = src[1] & 0xFF;
        int s3 = src[2] & 0xFF;
        int s4 = src[3] & 0xFF;

        return ((s1 << 24)+(s2<<16)+(s3<<8)+(s4<<0));
    }
}
