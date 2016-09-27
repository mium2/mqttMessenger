package com.msp.messenger.bean;

/**
 * Created by Y.B.H(mium2) on 16. 6. 13..
 */
public class UserInfoBean {
    private String APPID = "";
    private String USERID = "";
    private String PASSWORD = "";
    private String HP_NUM = "";
    private String DEVICEID = "";
    private String NICKNAME = "";
    private String BROKER_ID = "";
    private String ORG_BROKER_ID = "";
    private String MPSN = "";
    private String PUSH_TOKEN = "";
    private String PUSH_SERVER = "";

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

    public String getHP_NUM() {
        return HP_NUM;
    }

    public void setHP_NUM(String _HP_NUM) {
        this.HP_NUM  = _HP_NUM.replaceAll("-", "").replaceAll(" ","");
    }

    public String getDEVICEID() {
        return DEVICEID;
    }

    public void setDEVICEID(String DEVICEID) {
        this.DEVICEID = DEVICEID;
    }

    public String getNICKNAME() {
        return NICKNAME;
    }

    public void setNICKNAME(String NICKNAME) {
        this.NICKNAME = NICKNAME;
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

    public String getMPSN() {
        return MPSN;
    }

    public void setMPSN(String MPSN) {
        this.MPSN = MPSN;
    }

    public String getPUSH_TOKEN() {
        return PUSH_TOKEN;
    }

    public void setPUSH_TOKEN(String PUSH_TOKEN) {
        this.PUSH_TOKEN = PUSH_TOKEN.trim();
    }

    public String getPUSH_SERVER() {
        return PUSH_SERVER;
    }

    public void setPUSH_SERVER(String PUSH_SERVER) {
        this.PUSH_SERVER = PUSH_SERVER;
    }

}
