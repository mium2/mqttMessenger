package com.msp.chat.core.mqtt.proto.messages;

/**
 * Placeholder for PUBACK message.
 * 
 * @author mium2(Y.B.H)
 */
public class PubAckMessage extends MessageIDMessage {
    private Integer subscribeCnt = 0;
    public PubAckMessage() {
        m_messageType = AbstractMessage.PUBACK;
    }

    public Integer getSubscribeCnt() {
        return subscribeCnt;
    }

    public void setSubscribeCnt(Integer subscribeCnt) {
        this.subscribeCnt = subscribeCnt;
    }
}
