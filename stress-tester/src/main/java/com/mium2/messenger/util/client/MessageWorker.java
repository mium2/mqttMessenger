package com.mium2.messenger.util.client;

import com.msp.chat.core.mqtt.proto.messages.PubAckMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Created by Y.B.H(mium2) on 16. 10. 4..
 */
public class MessageWorker implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger("com.mium2.messenger.util");
    @Override
    public void run() {
        while(true) {
            MessageInfo messageInfo = (MessageInfo)MessageQueue.getMessage();
            if(messageInfo == null ) {
                try {
                    Thread.sleep(100);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                PublishMessage publishMessage = messageInfo.getPublishMessage();
                ChannelHandlerContext channelHandlerContext = messageInfo.getCtx();
                PubAckMessage puback = new PubAckMessage();
                puback.setMessageID(publishMessage.getMessageID());
                channelHandlerContext.channel().writeAndFlush(puback);
                String clientId = BrokerConnectManager.getInstance().getClientID(channelHandlerContext.channel().id().asShortText());
                logger.info("[{}] : {}",clientId,payload2Str(publishMessage.getPayload()));
            }catch(Exception e) {
                e.printStackTrace();
            }

        }
    }


    private String  payload2Str(ByteBuffer content) {
        byte[] b = new byte[content.remaining()];
        content.mark();
        content.get(b);
        content.reset();
        try {
            return new String(b,"utf-8");
        }catch (Exception e){
            e.printStackTrace();
        }
        return new String(b);
    }
}
