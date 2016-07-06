package com.msp.messenger.bean;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-06-24
 * Time: 오후 4:50
 * To change this template use File | Settings | File Templates.
 */
public class ServerGroupInfoBean {
    private String GROUPID = "";
    private String GROUPNAME ="";
    private String SERVERTYPE="";
    private String VIP="";
    private String PORT="";

    public String getGROUPID() {
        return GROUPID;
    }

    public void setGROUPID(String GROUPID) {
        this.GROUPID = GROUPID;
    }

    public String getGROUPNAME() {
        return GROUPNAME;
    }

    public void setGROUPNAME(String GROUPNAME) {
        this.GROUPNAME = GROUPNAME;
    }

    public String getSERVERTYPE() {
        return SERVERTYPE;
    }

    public void setSERVERTYPE(String SERVERTYPE) {
        this.SERVERTYPE = SERVERTYPE;
    }

    public String getVIP() {
        return VIP;
    }

    public void setVIP(String VIP) {
        this.VIP = VIP;
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
                "\"GROUPID\":\"" + GROUPID + "\"" +
                ", \"GROUPNAME\":\"" + GROUPNAME + "\"" +
                ", \"SERVERTYPE\":\"" + SERVERTYPE + "\"" +
                ", \"VIP\":\"" + VIP + "\"" +
                ", \"PORT\":\"" + PORT + "\"" +
                "}";
    }
}
