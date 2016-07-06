package com.msp.messenger.auth.vo;

/**
 * Created by Y.B.H(mium2) on 16. 6. 20..
 */
public class ReqAuthBaseVO {
    private String APPID = "";
    private String DEVICEID = "";
    private String USERID = "";
    private String MPSN = "";
    private String REDIRECT_URI = "";

    public ReqAuthBaseVO() {
        super();
    }

    public ReqAuthBaseVO(String APPID, String USERID, String MPSN,
                         String REDIRECT_URI) {
        super();
        this.APPID = APPID;
        this.USERID = USERID;
        this.MPSN = MPSN;
        this.REDIRECT_URI = REDIRECT_URI;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getDEVICEID() {
        return DEVICEID;
    }

    public void setDEVICEID(String DEVICEID) {
        this.DEVICEID = DEVICEID;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getMPSN() {
        return MPSN;
    }

    public void setMPSN(String MPSN) {
        this.MPSN = MPSN;
    }

    public String getREDIRECT_URI() {
        return REDIRECT_URI;
    }

    public void setREDIRECT_URI(String REDIRECT_URI) {
        this.REDIRECT_URI = REDIRECT_URI;
    }
}
