package com.msp.chat.server.bean;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-04-21
 * Time: 오전 10:46
 * To change this template use File | Settings | File Templates.
 */
public class ServerInfoBean {
    private String SERVERID = "";
    private String IP = "";
    private String PORT = "";

    public String getSERVERID() {
        return SERVERID;
    }

    public void setSERVERID(String SERVERID) {
        this.SERVERID = SERVERID;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getPORT() {
        return PORT;
    }

    public void setPORT(String PORT) {
        this.PORT = PORT;
    }

    @Override
    public String toString() {
        return "{" +
                "\"SERVERID\":\"" + SERVERID + "\"" +
                ", \"IP\":\"" + IP + "\"" +
                ", \"PORT\":\"" + PORT + "\"" +
                "}";
    }
}
