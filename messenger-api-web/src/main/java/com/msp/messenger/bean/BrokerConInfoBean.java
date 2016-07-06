package com.msp.messenger.bean;

/**
 * Created by Y.B.H(mium2) on 2016. 2. 22..
 */
public class BrokerConInfoBean {
    private String SERVERID = "";
    private String IP = "";
    private String PORT = "";
    private boolean isGROUP = false;

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

    public boolean isGROUP() {
        return isGROUP;
    }

    public void setIsGROUP(boolean isGROUP) {
        this.isGROUP = isGROUP;
    }
}
