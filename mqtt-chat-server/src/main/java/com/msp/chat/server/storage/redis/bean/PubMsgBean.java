package com.msp.chat.server.storage.redis.bean;


/**
 * Created by Y.B.H(mium2) on 16. 5. 16..
 */
public class PubMsgBean {

    private String topic;
    private String pub_qos;
    private String pub_message;
    private String retainYN = "N";
    private String subClientID;
    private String pubClientID;
    private String msgID;
    private String subscriberCnt= "0";
    private String sendate="0";

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPub_qos() {
        return pub_qos;
    }

    public void setPub_qos(String pub_qos) {
        this.pub_qos = pub_qos;
    }

    public String getPub_message() {
        return pub_message;
    }

    public void setPub_message(String pub_message) {
        this.pub_message = pub_message;
    }

    public String getRetainYN() {
        return retainYN;
    }

    public void setRetainYN(String retainYN) {
        this.retainYN = retainYN;
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

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getSubscriberCnt() {
        return subscriberCnt;
    }

    public void setSubscriberCnt(String subscriberCnt) {
        this.subscriberCnt = subscriberCnt;
    }

    public String getSendate() {
        return sendate;
    }

    public void setSendate(String sendate) {
        this.sendate = sendate;
    }

    @Override
    public String toString() {
        return "{" +
            "\"msgID\":\"" + msgID + "\"" +
            "\"topic\":\"" + topic + "\"" +
            "\"pub_qos\":\"" + pub_qos + "\"" +
            ", \"pub_message\":\"" + pub_message + "\"" +
            ", \"retainYN\":\"" + retainYN + "\"" +
            ", \"subClientID\":\"" + subClientID + "\"" +
            ", \"pubClientID\":\"" + pubClientID + "\"" +
            ", \"subscriberCnt\":\"" + subscriberCnt + "\"" +
            ", \"sendate\":\"" + sendate + "\"" +
            "}";
    }
}
