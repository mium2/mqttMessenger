package com.mium2.messenger.util;

import com.mium2.messenger.util.client.BrokerConnectManager;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by Y.B.H(mium2) on 16. 9. 27..
 */
public class AutoMsgSendThread extends Thread {

    private Logger logger = LoggerFactory.getLogger("com.mium2.messenger.util");
    private final long sleepTime;

    public AutoMsgSendThread(String threadName){
        super.setName(threadName);
        this.sleepTime = ConfigLoader.getIntProperty(ConfigLoader.AUTOSENDINTERVAL);
    }

    @Override
    public void run() {
        while(true) {
            // 생성되어 있는 랜덤한 ROOMID을 가져온다.
            String randomRoomIdSenderId = StressTester.getInstance().getRandomRoomIDSenderIdMap();
            String[] randomRoomIdSenderIdArr = randomRoomIdSenderId.split("\\|");
            String roomID = randomRoomIdSenderIdArr[0];
            String senderID = randomRoomIdSenderIdArr[1];

            logger.debug("## roomID:" + roomID + "     senderID:" + senderID);

            ChannelFuture channelFuture = BrokerConnectManager.getInstance().getChannelFuture(senderID);

            if(channelFuture!=null && channelFuture.channel().isActive()){
                try {
                    byte[] sendMsgBytes ="하하하 성능 테스트 입니다.".getBytes("utf-8");
                    ByteBuffer sendMsgBuffer = ByteBuffer.allocate(sendMsgBytes.length);
                    sendMsgBuffer.put(sendMsgBytes);
                    sendMsgBuffer.flip();

                    PublishMessage pubMessage = new PublishMessage();
                    pubMessage.setRetainFlag(false);
                    pubMessage.setTopicName(roomID);
                    pubMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);
                    pubMessage.setPayload(sendMsgBuffer);
                    pubMessage.setMessageID(MsgIDManager.getInstance().getNextMessgeID());

                    channelFuture.channel().writeAndFlush(pubMessage);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
