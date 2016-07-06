package com.msp.messenger.auth.vo;

import java.util.List;

public class ClientVO {
	private String CLIENT_ID;
	private String CLEINT_SECRET;
	private String USERID;
	private String CLIENT_NAME;
	private String DESCRIPTION;
	private String CLIENT_URL;
	private String CLIENT_TYPE;
	private List<String> SCOPES;
	private String SCOPE;
	private String REDIRECT_URI;
	private String REGDATE;
	
	public ClientVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ClientVO(String client_id, String client_secret, String userid,
			String client_name, String description, String client_url,
			String client_type, String scope, String redirect_uri, String regdate) {
		super();
		this.CLIENT_ID = client_id;
		this.CLEINT_SECRET = client_secret;
		this.USERID = userid;
		this.CLIENT_NAME = client_name;
		this.DESCRIPTION = description;
		this.CLIENT_URL = client_url;
		this.CLIENT_TYPE = client_type;
		this.SCOPE = scope;
		this.REDIRECT_URI = redirect_uri;
		this.REGDATE = regdate;
	}

    public String getCLIENT_ID() {
        return CLIENT_ID;
    }

    public void setCLIENT_ID(String CLIENT_ID) {
        this.CLIENT_ID = CLIENT_ID;
    }

    public String getCLEINT_SECRET() {
        return CLEINT_SECRET;
    }

    public void setCLEINT_SECRET(String CLEINT_SECRET) {
        this.CLEINT_SECRET = CLEINT_SECRET;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getCLIENT_NAME() {
        return CLIENT_NAME;
    }

    public void setCLIENT_NAME(String CLIENT_NAME) {
        this.CLIENT_NAME = CLIENT_NAME;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION;
    }

    public void setDESCRIPTION(String DESCRIPTION) {
        this.DESCRIPTION = DESCRIPTION;
    }

    public String getCLIENT_URL() {
        return CLIENT_URL;
    }

    public void setCLIENT_URL(String CLIENT_URL) {
        this.CLIENT_URL = CLIENT_URL;
    }

    public String getCLIENT_TYPE() {
        return CLIENT_TYPE;
    }

    public void setCLIENT_TYPE(String CLIENT_TYPE) {
        this.CLIENT_TYPE = CLIENT_TYPE;
    }

    public List<String> getSCOPES() {
        return SCOPES;
    }

    public void setSCOPES(List<String> SCOPES) {
        this.SCOPES = SCOPES;
    }

    public String getSCOPE() {
        return SCOPE;
    }

    public void setSCOPE(String SCOPE) {
        this.SCOPE = SCOPE;
    }

    public String getREDIRECT_URI() {
        return REDIRECT_URI;
    }

    public void setREDIRECT_URI(String REDIRECT_URI) {
        this.REDIRECT_URI = REDIRECT_URI;
    }

    public String getREGDATE() {
        return REGDATE;
    }

    public void setREGDATE(String REGDATE) {
        this.REGDATE = REGDATE;
    }

    @Override
	public String toString() {
		return "ClientVO [CLIENT_ID=" + CLIENT_ID + ", CLEINT_SECRET="
				+ CLEINT_SECRET + ", USERID=" + USERID + ", CLIENT_NAME="
				+ CLIENT_NAME + ", DESCRIPTION=" + DESCRIPTION
				+ ", CLIENT_URL=" + CLIENT_URL + ", CLIENT_TYPE=" + CLIENT_TYPE
				+ ", SCOPES=" + SCOPES + ", REDIRECT_URI=" + REDIRECT_URI
				+ ", REGDATE=" + REGDATE + "]";
	}
}
