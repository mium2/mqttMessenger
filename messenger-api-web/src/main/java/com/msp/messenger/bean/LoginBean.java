package com.msp.messenger.bean;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 19..
 */
public class LoginBean {
    private String APPID = "";
    private String USERID = "";
    private String PASSWORD = "";
    private String DEVICEID = "";
    private String PUSH_TOKEN = "";
    private String PUSH_SERVER = "";
    private String APNS_MODE = "";

    private String BROKER_ID = "";
    private String ORG_BROKER_ID = "";

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public String getDEVICEID() {
        return DEVICEID;
    }

    public void setDEVICEID(String DEVICEID) {
        this.DEVICEID = DEVICEID;
    }

    public String getPUSH_TOKEN() {
        return PUSH_TOKEN;
    }

    public void setPUSH_TOKEN(String PUSH_TOKEN) {
        this.PUSH_TOKEN = PUSH_TOKEN;
    }

    public String getPUSH_SERVER() {
        return PUSH_SERVER;
    }

    public void setPUSH_SERVER(String PUSH_SERVER) {
        this.PUSH_SERVER = PUSH_SERVER;
    }

    public String getAPNS_MODE() {
        return APNS_MODE;
    }

    public void setAPNS_MODE(String APNS_MODE) {
        this.APNS_MODE = APNS_MODE;
    }

    public String getBROKER_ID() {
        return BROKER_ID;
    }

    public void setBROKER_ID(String BROKER_ID) {
        this.BROKER_ID = BROKER_ID;
    }

    public String getORG_BROKER_ID() {
        return ORG_BROKER_ID;
    }

    public void setORG_BROKER_ID(String ORG_BROKER_ID) {
        this.ORG_BROKER_ID = ORG_BROKER_ID;
    }
}
