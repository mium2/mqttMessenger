package com.msp.messenger.bean;

/**
 * Created by Y.B.H(mium2) on 16. 6. 21..
 */
public class MakeRoomBean {
    private String AUTHKEY = "";
    private String APPID = "";
    private String USERID = "";
    private String INVITE_USERIDS = "";
    private String ROOMID = "";
    private String ROOMNAME = "";
    private String ROOMTYPE = "";


    public String getAUTHKEY() {
        return AUTHKEY;
    }

    public void setAUTHKEY(String AUTHKEY) {
        this.AUTHKEY = AUTHKEY;
    }

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

    public String getINVITE_USERIDS() {
        return INVITE_USERIDS;
    }

    public void setINVITE_USERIDS(String INVITE_USERIDS) {
        this.INVITE_USERIDS = INVITE_USERIDS;
    }

    public String getROOMID() {
        return ROOMID;
    }

    public void setROOMID(String ROOMID) {
        this.ROOMID = ROOMID;
    }

    public String getROOMTYPE() {
        return ROOMTYPE;
    }

    public void setROOMTYPE(String ROOMTYPE) {
        this.ROOMTYPE = ROOMTYPE;
    }

    public String getROOMNAME() {
        return ROOMNAME;
    }

    public void setROOMNAME(String ROOMNAME) {
        this.ROOMNAME = ROOMNAME;
    }
}
