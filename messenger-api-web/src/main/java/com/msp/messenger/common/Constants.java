package com.msp.messenger.common;

public class Constants {
	public static final String HEADER_KEY = "HEADER";
	public static final String BODY_KEY = "BODY";
	public static final String RESULT_CODE_KEY = "RESULT_CODE";
	public static final String RESULT_BODY_KEY = "RESULT_BODY";
	public static final String RESULT_MESSAGE_KEY = "RESULT_BODY";
    public static final String HEADER_SERVICE = "SERVICE";

	public static final String RESULT_CODE_OK = "200";
	public static final String RESULT_MESSAGE_OK = "OK";

	public static final String PARAM_HASH_SUFFIX = "_hs";
	public static final String PARAM_MD5_SUFFIX = "_md5";
	
	public static final String PARAM_PLAIN_SUFFIX = "_pl";
	public static final String PARAM_CRYPT_SUFFIX = "_cr";
	
	public static final String DEFAULT_SOUND_FILE	= "beep.aif";
	public static final String DEFAULT_BADGENO		= "0";
	
	public static final String CSV_SEPERATOR_REGEX		= "[\t]";

	public static final String ERR_500 = "500";
	public static final String ERR_500_MSG = "서버에러";

	public static final String ERR_1000 = "1000";
	public static final String ERR_1000_MSG = "필수 파라미터에러.";

	public static final String ERR_1001 = "1001";
	public static final String ERR_1001_MSG = "파라미터에러.";

	public static final String ERR_1002 = "1002";
	public static final String ERR_1002_MSG = "존재하지 않는 사용자아이디 입니다.";

	public static final String ERR_2000 = "2000";
	public static final String ERR_2000_MSG = "핸드폰번호 인증에러";

    public static final String ERR_2001 = "2001";
    public static final String ERR_2001_MSG = "이미 사용중인 핸드폰번호 입니다.";

    public static final String ERR_2002 = "2002";
    public static final String ERR_2002_MSG = "등록되어 있는 핸드폰번호와 다릅니다";

	public static final String ERR_2003 = "2003";
	public static final String ERR_2003_MSG = "이미 존재하는 대화방입니다.";

	public static final String ERR_3000 = "3000";
	public static final String ERR_3000_MSG = "인증에러";

	public static final String ERR_3001 = "3001";
	public static final String ERR_3001_MSG = "사용자 인증에러";

    public static final String ERR_3002 = "3002";
    public static final String ERR_3002_MSG = "인증키가 존재하지 않습니다.";

    public static final String ERR_3003 = "3003";
    public static final String ERR_3003_MSG = "인증해야 할 앱아이디가 존재하지 않습니다.";

    public static final String ERR_3004 = "3004";
    public static final String ERR_3004_MSG = "인증키가 위변조 되었습니다.";

    public static final String ERR_3005 = "3005";
    public static final String ERR_3005_MSG = "인증이 만료되었습니다.";

    public static final String ERR_3006 = "3006";
    public static final String ERR_3006_MSG = "인증정보가 일치하지 않습니다.";

	public static final String ERR_3007 = "3007";
	public static final String ERR_3007_MSG = "인증토큰이 올바르지 않습니다.";

	public static final String ERR_3008 = "3008";
	public static final String ERR_3008_MSG = "인증해야 할 사용자 아이디가 존재하지 않습니다.";

	public static final String ERR_4000 = "4000";
	public static final String ERR_4000_MSG = "라이센스 에러";

	public static final String ERR_4001 = "4001";
	public static final String ERR_4001_MSG = "해댱 앱아이디는 라이센스가 없습니다.";

	public static final String ERR_4002 = "4002";
	public static final String ERR_4002_MSG = "라이센스 정보가 올바르지 않습니다.";

	public static final String ERR_5000 = "5000";
	public static final String ERR_5000_MSG = "푸시서비스 가입 에러.";

}
