package com.msp.chat.server.storage.redis.bean;

/**
 * Created by Y.B.H(mium2) on 16. 5. 11..
 */
public class OffMsgBean {

    private String msgID = "";
    private String pub_message;
    private String pubClientID = "";
    private String subClientID = "";
    private String retain = "";
    private String qos = "";
    private String topic = "";
    private long expire = 0;

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }


    public String getPub_message() {
        return pub_message;
    }

    public void setPub_message(String pub_message) {
        this.pub_message = pub_message;
    }

    public String getPubClientID() {
        return pubClientID;
    }

    public void setPubClientID(String pubClientID) {
        this.pubClientID = pubClientID;
    }

    public String getSubClientID() {
        return subClientID;
    }

    public void setSubClientID(String subClientID) {
        this.subClientID = subClientID;
    }

    public String getRetain() {
        return retain;
    }

    public void setRetain(String retain) {
        this.retain = retain;
    }

    public String getQos() {
        return qos;
    }

    public void setQos(String qos) {
        this.qos = qos;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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
            "\"msgID\":\"" + msgID + "\"" +
            ", \"pubClientID\":\"" + pubClientID + "\"" +
            ", \"subClientID\":\"" + subClientID + "\"" +
            ", \"retain\":\"" + retain + "\"" +
            ", \"qos\":\"" + qos + "\"" +
            ", \"topic\":\"" + topic + "\"" +
            ", \"expire\":" + expire +
            "}";
    }
}
