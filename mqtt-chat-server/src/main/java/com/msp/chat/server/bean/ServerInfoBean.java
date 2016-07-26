package com.msp.chat.server.bean;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
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
