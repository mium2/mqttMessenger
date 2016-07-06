package com.msp.messenger.bean;

/**
 * Created by Y.B.H(mium2) on 16. 6. 17..
 */
public class FriendModelBean {
    private String APPID = "";
    private String USERID = "";
    private String FRIEND_HPLIST = "";

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

    public String getFRIEND_HPLIST() {
        return FRIEND_HPLIST;
    }

    public void setFRIEND_HPLIST(String FRIEND_HPLIST) {
        this.FRIEND_HPLIST = FRIEND_HPLIST;
    }
}
