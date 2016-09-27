package com.msp.chat.license;

/**
 * Created by Y.B.H(mium2) on 16. 6. 17..
 */
public class AppLicenseBean {
    private String APPID = "";
    private String MPSN_KEY  = "";
    private String SECRET_KEY = "";
    private String USERID = "";

    public AppLicenseBean(String APPID, String MPSN_KEY, String SECRET_KEY){
        this.APPID = APPID;
        this.MPSN_KEY = MPSN_KEY;
        this.SECRET_KEY = SECRET_KEY;
    }
    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getMPSN_KEY() {
        return MPSN_KEY;
    }

    public void setMPSN_KEY(String MPSN_KEY) {
        this.MPSN_KEY = MPSN_KEY;
    }

    public String getSECRET_KEY() {
        return SECRET_KEY;
    }

    public void setSECRET_KEY(String SECRET_KEY) {
        this.SECRET_KEY = SECRET_KEY;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }
}
