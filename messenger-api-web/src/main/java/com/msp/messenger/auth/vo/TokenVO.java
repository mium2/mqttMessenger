//CREATE TABLE tbl_token 
//(
//	client_id      		varchar2(100)  NOT NULL,		
//	userid				varchar2(20) NOT NULL,
//	access_token		varchar2(200) unique,
//	refresh_token		varchar2(200) unique,
//	token_type			varchar2(30),				--bearer,jwt,mac
//	scope				varchar2(100),				--���� : �б�,�б⾲�� ���� ������� �����Ͽ� ����ϴ� ���� ǥ��
//	created_at			number(30),						--access token ����� timestamp
//	created_rt			number(30),						--refresh token ����� timestamp
//	expires_in			number(30)						--��ū ��ÿ� �ο��ϴ� ��ȿ�Ⱓ 3600
//);

package com.msp.messenger.auth.vo;

public class TokenVO {
	private String CLIENT_ID;
	private String USERID;
	private String ACCESS_TOKEN;
	private String REFRESH_TOKEN;
	private String TOKEN_TYPE;
	private String SCOPE;
	private String CODE;
	private String STATE;
	private String CLIENT_TYPE;
	private long CREATE_AT;
	private long CREATE_RT;
	private long EXPIRES_IN;
	
	public TokenVO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TokenVO(String client_id, String userid, String access_token,
			String refresh_token, String token_type, String scope, String client_type,
			long created_at, long created_rt, long expires_in) {
		super();
		this.CLIENT_ID = client_id;
		this.USERID = userid;
		this.ACCESS_TOKEN = access_token;
		this.REFRESH_TOKEN = refresh_token;
		this.TOKEN_TYPE = token_type;
		this.SCOPE = scope;
		this.CLIENT_TYPE = client_type;
		this.CREATE_AT = created_at;
		this.CREATE_RT = created_rt;
		this.EXPIRES_IN = expires_in;
	}

    public String getCLIENT_ID() {
        return CLIENT_ID;
    }

    public void setCLIENT_ID(String CLIENT_ID) {
        this.CLIENT_ID = CLIENT_ID;
    }

    public String getUSERID() {
        return USERID;
    }

    public void setUSERID(String USERID) {
        this.USERID = USERID;
    }

    public String getACCESS_TOKEN() {
        return ACCESS_TOKEN;
    }

    public void setACCESS_TOKEN(String ACCESS_TOKEN) {
        this.ACCESS_TOKEN = ACCESS_TOKEN;
    }

    public String getREFRESH_TOKEN() {
        return REFRESH_TOKEN;
    }

    public void setREFRESH_TOKEN(String REFRESH_TOKEN) {
        this.REFRESH_TOKEN = REFRESH_TOKEN;
    }

    public String getTOKEN_TYPE() {
        return TOKEN_TYPE;
    }

    public void setTOKEN_TYPE(String TOKEN_TYPE) {
        this.TOKEN_TYPE = TOKEN_TYPE;
    }

    public String getSCOPE() {
        return SCOPE;
    }

    public void setSCOPE(String SCOPE) {
        this.SCOPE = SCOPE;
    }

    public String getCODE() {
        return CODE;
    }

    public void setCODE(String CODE) {
        this.CODE = CODE;
    }

    public String getSTATE() {
        return STATE;
    }

    public void setSTATE(String STATE) {
        this.STATE = STATE;
    }

    public String getCLIENT_TYPE() {
        return CLIENT_TYPE;
    }

    public void setCLIENT_TYPE(String CLIENT_TYPE) {
        this.CLIENT_TYPE = CLIENT_TYPE;
    }

    public long getCREATE_AT() {
        return CREATE_AT;
    }

    public void setCREATE_AT(long CREATE_AT) {
        this.CREATE_AT = CREATE_AT;
    }

    public long getCREATE_RT() {
        return CREATE_RT;
    }

    public void setCREATE_RT(long CREATE_RT) {
        this.CREATE_RT = CREATE_RT;
    }

    public long getEXPIRES_IN() {
        return EXPIRES_IN;
    }

    public void setEXPIRES_IN(long EXPIRES_IN) {
        this.EXPIRES_IN = EXPIRES_IN;
    }

    @Override
	public String toString() {
		return "TokenVO [CLIENT_ID=" + CLIENT_ID + ", USERID=" + USERID
				+ ", ACCESS_TOKEN=" + ACCESS_TOKEN + ", REFRESH_TOKEN="
				+ REFRESH_TOKEN + ", TOKEN_TYPE=" + TOKEN_TYPE + ", SCOPE="
				+ SCOPE + ", CODE=" + CODE + ", STATE=" + STATE
				+ ", CLIENT_TYPE=" + CLIENT_TYPE + ", CREATE_AT=" + CREATE_AT
				+ ", CREATE_RT=" + CREATE_RT + ", EXPIRES_IN=" + EXPIRES_IN
				+ "]";
	}
	
	
	
}
