package com.msp.chat.server.bean;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage.QOSType;

import java.io.Serializable;

/**
 * Maintain the information about which Topic a certain ClientID is subscribed 
 * and at which QoS
 *
 */
public class Subscription implements Serializable {
    
    int qos;
    QOSType requestedQos;
    String clientId;
    String topic;
    boolean cleanSession;
    boolean active = true;
    String cleanSessionYN = "Y";
    String activeYN = "N";

    public Subscription(){}
    public Subscription(String clientId, String topic, QOSType requestedQos, boolean cleanSession) {
        this.qos = requestedQos.ordinal();
        this.requestedQos = requestedQos;
        this.clientId = clientId;
        this.topic = topic;
        this.cleanSession = cleanSession;
        if(cleanSession){
            cleanSessionYN = "Y";
        }else{
            cleanSessionYN = "N";
        }
        if(active){
            activeYN = "Y";
        }else{
            activeYN = "N";
        }
    }

    public String getClientId() {
        return clientId;
    }

    public QOSType getRequestedQos() {
        return requestedQos;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isCleanSession() {
        return this.cleanSession;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getQos() {
        return qos;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setQos(int qos) {
        this.qos = qos;
        this.requestedQos = AbstractMessage.QOSType.values()[qos];
    }

    public String getActiveYN() {
        return activeYN;
    }

    public void setActiveYN(String activeYN) {
        this.activeYN = activeYN;
        if(activeYN.equals("Y")){
            active = true;
        }else{
            active = false;
        }
    }

    public String getCleanSessionYN() {
        return cleanSessionYN;
    }

    public void setCleanSessionYN(String cleanSessionYN) {
        this.cleanSessionYN = cleanSessionYN;
        if(cleanSessionYN.equals("Y")){
            cleanSession = true;
        }else{
            cleanSession = false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Subscription other = (Subscription) obj;
        if (this.requestedQos != other.requestedQos) {
            return false;
        }
        if ((this.clientId == null) ? (other.clientId != null) : !this.clientId.equals(other.clientId)) {
            return false;
        }
        if ((this.topic == null) ? (other.topic != null) : !this.topic.equals(other.topic)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.requestedQos != null ? this.requestedQos.hashCode() : 0);
        hash = 37 * hash + (this.clientId != null ? this.clientId.hashCode() : 0);
        hash = 37 * hash + (this.topic != null ? this.topic.hashCode() : 0);
        return hash;
    }

    /**
     * Trivial match method
     */
    boolean match(String topic) {
        return this.topic.equals(topic);
    }
    
    @Override
    public String toString() {
        return String.format("[t:%s, cliID: %s, qos: %s, active: %s]", this.topic, this.clientId, this.requestedQos, this.active);
    }
}
