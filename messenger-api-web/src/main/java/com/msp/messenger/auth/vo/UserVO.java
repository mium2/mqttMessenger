package com.msp.messenger.auth.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserVO {
	private String USERID;
	private String PASSWORD;
	private String USERNAME;
	
	public UserVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserVO(String userid, String password, String username) {
		super();
		this.USERID = userid;
		this.PASSWORD = password;
		this.USERNAME = username;
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

    public String getUSERNAME() {
        return USERNAME;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    @Override
	public String toString() {
		return "UserVO [USERID=" + USERID + ", PASSWORD=" + PASSWORD + ", USERNAME=" + USERNAME + "]";
	}
}
