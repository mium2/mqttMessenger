package com.msp.messenger.bean;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Y.B.H(mium2) on 16. 6. 21..
 */
public class ChatRoomInfoBean {
    private String APPID = "";
    private String ROOMID= "";
    private String ROOM_OWNER = "";
    private String ROOM_NAME = "";
    private List<Object> USERIDS;
    private List<Object> HPNUMS;

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getROOMID() {
        return ROOMID;
    }

    public void setROOMID(String ROOMID) {
        this.ROOMID = ROOMID;
    }

    public String getROOM_OWNER() {
        return ROOM_OWNER;
    }

    public void setROOM_OWNER(String ROOM_OWNER) {
        this.ROOM_OWNER = ROOM_OWNER;
    }

    public String getROOM_NAME() {
        return ROOM_NAME;
    }

    public void setROOM_NAME(String ROOM_NAME) {
        this.ROOM_NAME = ROOM_NAME;
    }

    public List<Object> getUSERIDS() {
        return USERIDS;
    }

    public void setUSERIDS(List<Object> USERIDS) {
        this.USERIDS = USERIDS;
    }

    public List<Object> getHPNUMS() {
        return HPNUMS;
    }

    public void setHPNUMS(List<Object> HPNUMS) {
        this.HPNUMS = HPNUMS;
    }
}
