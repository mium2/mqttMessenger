package com.msp.chat.server.bean.events;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.server.commons.utill.DebugUtils;
import com.msp.chat.server.netty.ServerChannel;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage.QOSType;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 */
public class PublishEvent extends MessagingEvent {
    String m_topic;
    QOSType m_qos;
    int pub_qos;
    //byte[] m_message;
    ByteBuffer m_message;
    String pub_message;
    boolean m_retain;
    String retainYN = "N";
    String subClientID;
    String pubClientID;
    //Optional attribute, available only fo QoS 1 and 2
    int m_msgID;
    long expire = 0;

    transient ServerChannel m_session;
    public PublishEvent(){}
    public PublishEvent(PublishMessage pubMsg,String pub_clientID, String sub_clientID, ServerChannel session) {
        m_topic = pubMsg.getTopicName();
        m_qos = pubMsg.getQos();
        pub_qos = pubMsg.getQos().ordinal();
        m_message = pubMsg.getPayload();

        ByteBuffer buffer = pubMsg.getPayload();
//        byte[] msgByte = new byte[buffer.remaining()];
        pub_message = DebugUtils.payload2Str(buffer);

        m_retain = pubMsg.isRetainFlag();
        pubClientID = pub_clientID;
        subClientID = sub_clientID;
        m_session = session;
        if (pubMsg.getQos() != QOSType.MOST_ONE) {
            m_msgID = pubMsg.getMessageID();
        }
        if(m_retain){
            retainYN = "Y";
        }else{
            retainYN = "N";
        }
    }
    
    public PublishEvent(String topic, QOSType qos, ByteBuffer message, boolean retain,String pub_clientID,String sub_clientID, ServerChannel session) {
        m_topic = topic;
        m_qos = qos;
        pub_qos = qos.ordinal();
        m_message = message;
        pub_message = new String(message.array());
        m_retain = retain;
        if(m_retain){
            retainYN = "Y";
        }else{
            retainYN = "N";
        }
        pubClientID = pub_clientID;
        subClientID = sub_clientID;
        m_session = session;
    }

    public PublishEvent(String topic, QOSType qos, ByteBuffer message, boolean retain,String pub_clientID,String sub_clientID, int msgID, ServerChannel session) {
        this(topic, qos, message, retain, pub_clientID, sub_clientID, session);
        m_msgID = msgID;
    }
    
    public String getTopic() {
        return m_topic;
    }

    public void setM_topic(String m_topic) {
        this.m_topic = m_topic;
    }

    public QOSType getQos() {
        return m_qos;
    }

    public ByteBuffer getMessage() {
        return m_message;
    }

    public boolean isRetain() {
        return m_retain;
    }

    public String getSubClientID() {
        return subClientID;
    }

    public void setSubClientID(String subClientID) {
        this.subClientID = subClientID;
    }

    public String getPubClientID() {
        return pubClientID;
    }

    public void setPubClientID(String pubClientID) {
        this.pubClientID = pubClientID;
    }

    public int getMessageID() {
        return m_msgID;
    }

    public void setM_msgID(int m_msgID) {
        this.m_msgID = m_msgID;
    }

    public ServerChannel getSession() {
        return m_session;
    }

    public int getPub_qos() {
        return pub_qos;
    }

    public void setPub_qos(int pub_qos) {
        this.pub_qos = pub_qos;
        this.m_qos = AbstractMessage.QOSType.values()[pub_qos];
    }

    public String getPub_message() {
        return pub_message;
    }

    public void setPub_message(String pub_message) {
        this.pub_message = pub_message;
        ByteBuffer b = ByteBuffer.wrap( pub_message.getBytes());
        this.m_message = b;
    }

    public String getRetainYN() {
        return retainYN;
    }

    public void setRetainYN(String retainYN) {
        this.retainYN = retainYN;
        if(retainYN.equals("Y")){
            m_retain = true;
        }else{
            m_retain = false;
        }
    }

    public ByteBuffer getM_message() {
        return m_message;
    }

    public void setM_message(ByteBuffer m_message) {
        this.m_message = m_message;
        this.pub_message = DebugUtils.payload2Str(m_message);
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    @Override
    public String toString() {
        return "{" +
            "\"msgID\":\"" + m_msgID + "\"" +
            ", \"pubClientID\":\"" + pubClientID + "\"" +
            ", \"subClientID\":\"" + subClientID + "\"" +
            ", \"retainYN\":\"" + retainYN + "\"" +
            ", \"pub_qos\":\"" + pub_qos + "\"" +
            ", \"pub_message\":\"" + pub_message + "\"" +
            ", \"topic\":\"" + m_topic + "\"" +
            ", \"expire\":\"" + expire + "\"" +
            "}";
    }
}
