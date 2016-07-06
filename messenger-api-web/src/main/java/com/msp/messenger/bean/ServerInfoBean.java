package com.msp.messenger.bean;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-04-21
 * Time: 오전 10:46
 * To change this template use File | Settings | File Templates.
 */
public class ServerInfoBean {
    private String SERVERID = "";
    private String GROUPID = "";
    private String IP = "";
    private String PORT = "";
    private String INTERNAL_IP = "";
    private String SERVERTYPE = "";
    private String SERVERNAME = "";
    private String URL = "";
    private String ISACTIVE = "N";

    public String getSERVERID() {
        return SERVERID;
    }

    public void setSERVERID(String SERVERID) {
        this.SERVERID = SERVERID;
    }

    public String getGROUPID() {
        return GROUPID;
    }

    public void setGROUPID(String GROUPID) {
        this.GROUPID = GROUPID;
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

    public String getINTERNAL_IP() {
        return INTERNAL_IP;
    }

    public void setINTERNAL_IP(String INTERNAL_IP) {
        this.INTERNAL_IP = INTERNAL_IP;
    }

    public String getSERVERTYPE() {
        return SERVERTYPE;
    }

    public void setSERVERTYPE(String SERVERTYPE) {
        this.SERVERTYPE = SERVERTYPE;
    }

    public String getSERVERNAME() {
        return SERVERNAME;
    }

    public void setSERVERNAME(String SERVERNAME) {
        this.SERVERNAME = SERVERNAME;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getISACTIVE() {
        return ISACTIVE;
    }

    public void setISACTIVE(String ISACTIVE) {
        this.ISACTIVE = ISACTIVE;
    }

    @Override
    public String toString() {
        return "{" +
                "\"SERVERID\":\"" + SERVERID + "\"" +
                ", \"GROUPID\":\"" + GROUPID + "\"" +
                ", \"IP\":\"" + IP + "\"" +
                ", \"PORT\":\"" + PORT + "\"" +
                ", \"SERVERTYPE\":\"" + SERVERTYPE + "\"" +
                ", \"SERVERNAME\":\"" + SERVERNAME + "\"" +
                ", \"ISACTIVE\":\"" + ISACTIVE + "\"" +
                "}";
    }
}
