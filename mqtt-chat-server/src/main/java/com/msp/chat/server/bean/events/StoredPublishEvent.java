package com.msp.chat.server.bean.events;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage.QOSType;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Publish event serialized to the DB.
 * 
 * @author andrea
 */
public class StoredPublishEvent implements Serializable {
    String m_topic;
    QOSType m_qos;
    int pub_qos;
    byte[] m_message;
    String pub_message;
    boolean m_retain;
    String retainYN = "N";
    String m_clientID;
    //Optional attribute, available only fo QoS 1 and 2
    int m_msgID;

    public StoredPublishEvent(){}
    
    public StoredPublishEvent(PublishEvent wrapped){
        m_topic = wrapped.getTopic();
        m_qos = wrapped.getQos();
        m_retain = wrapped.isRetain();
        m_clientID = wrapped.getPubClientID();
        m_msgID = wrapped.getMessageID();
        pub_qos = wrapped.getQos().ordinal();
        
        ByteBuffer buffer = wrapped.getMessage();
        m_message = new byte[buffer.remaining()];
        pub_message = new String(buffer.array());
        if(m_retain){
            retainYN = "Y";
        }else{
            retainYN = "N";
        }
        buffer.get(m_message);
        buffer.rewind();
    }
    
    public String getTopic() {
        return m_topic;
    }

    public void setTopic(String _topic){
        this.m_topic = _topic;
    }

    public QOSType getQos() {
        return m_qos;
    }

    public byte[] getMessage() {
        return m_message;
    }

    public void setMessage(ByteBuffer buffer){
        this.m_message = new byte[buffer.remaining()];
        this.pub_message = new String(buffer.array());
    }

    public boolean isRetain() {
        return m_retain;
    }

    public void setRetain(boolean _retain){
        this.m_retain = _retain;
        if(this.m_retain){
            retainYN = "Y";
        }else{
            retainYN = "N";
        }
    }
    
    public String getClientID() {
        return m_clientID;
    }

    public void setClientID(String _clientID){
        this.m_clientID = _clientID;
    }

    public int getMessageID() {
        return m_msgID;
    }

    public void setMessageID(int _msgID){
        this.m_msgID = _msgID;
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
        this.m_message = pub_message.getBytes();
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
}
