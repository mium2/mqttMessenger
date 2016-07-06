package com.msp.chat.server.storage.redis.bean;

import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by Y.B.H(mium2) on 16. 4. 20..
 */
public class RedisChatRoomBean {
    private String appID = "";
    private String roomID = "";
    private String alias = "";
    private HashSet<String> useridSet = new HashSet<String>();

    public RedisChatRoomBean(String appID, String roomID, String alias, String userids){
        this.appID = appID;
        this.roomID = roomID;
        this.alias = alias;
        StringTokenizer st = new StringTokenizer(userids,",");
        while (st.hasMoreTokens()){
            String userID = st.nextToken().trim();
            if(userID!=null && !"".equals(userID)) {
                useridSet.add(userID);
            }
        }
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public HashSet<String> getUseridSet() {
        return useridSet;
    }

    public void setUseridSet(HashSet<String> useridSet) {
        this.useridSet = useridSet;
    }

    @Override
    public String toString() {
        return "RedisChatRoomBean{" +
            "appID='" + appID + '\'' +
            ", roomID='" + roomID + '\'' +
            ", alias='" + alias + '\'' +
            ", useridSet=" + useridSet +
            '}';
    }
}
