package com.msp.messenger.common;

public class Constants {
	public static final String TEMP_AUTH_USERID = "TMP_GUEST";
	public static final String HEADER_KEY = "HEADER";
	public static final String BODY_KEY = "BODY";
	public static final String RESULT_CODE_KEY = "RESULT_CODE";
	public static final String RESULT_BODY_KEY = "RESULT_BODY";
	public static final String RESULT_MESSAGE_KEY = "RESULT_BODY";
    public static final String HEADER_SERVICE = "SERVICE";

	public static final String RESULT_CODE_OK = "200";
	public static final String RESULT_MESSAGE_OK = "OK";
	public static final String RESULT_CODE_HOLD_OK = "205"; //해당 토큰은 서비스 가입시에만 유효하도록 처리.

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

	public static final String ERR_1003 = "1003";
	public static final String ERR_1003_MSG = "이미 사용중인 아이디 입니다.";

	public static final String ERR_2000 = "2000";
	public static final String ERR_2000_MSG = "핸드폰번호 인증에러";

    public static final String ERR_2001 = "2001";
    public static final String ERR_2001_MSG = "이미 사용중인 핸드폰번호 입니다.";

    public static final String ERR_2002 = "2002";
    public static final String ERR_2002_MSG = "등록되어 있는 핸드폰번호와 다릅니다";

	public static final String ERR_2003 = "2003";
	public static final String ERR_2003_MSG = "이미 존재하는 대화방입니다.";

	public static final String ERR_2004 = "2004";
	public static final String ERR_2004_MSG = "대화방 타입이 올바르지 않습니다.";

	public static final String ERR_2005 = "2005";
	public static final String ERR_2005_MSG = "1:1대화방은 한명의 유저만 초대 가능합니다.";

	public static final String ERR_2006 = "2006";
	public static final String ERR_2006_MSG = "초대하려는 아이디는 서비스 가입이되어 있지 않습니다.";

	public static final String ERR_2007 = "2007";
	public static final String ERR_2007_MSG = "존재하지 않는 대화방입니다.";

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
    public static final String ERR_3005_MSG = "토큰이 만료되어 인증에 실패하였습니다.";

    public static final String ERR_3006 = "3006";
    public static final String ERR_3006_MSG = "인증정보가 일치하지 않습니다.";

	public static final String ERR_3007 = "3007";
	public static final String ERR_3007_MSG = "인증토큰이 올바르지 않습니다.";

	public static final String ERR_3008 = "3008";
	public static final String ERR_3008_MSG = "인증해야 할 사용자 아이디가 존재하지 않습니다.";

	public static final String ERR_3009 = "3009";
	public static final String ERR_3009_MSG = "아이디/패스워드가 일치하지 않습니다.";

	public static final String ERR_3010 = "3010";
	public static final String ERR_3010_MSG = "인증실패. 로그인을 다시해 주세요.";

	public static final String ERR_3011 = "3011";
	public static final String ERR_3011_MSG = "인증실패. 디바이스 고유아이디를 확인해 주세요.";

	public static final String ERR_3012 = "3012";
	public static final String ERR_3012_MSG = "해당 디바이스에서 사용 할 수 없는 토큰입니다.";

	public static final String ERR_3013 = "3013";
	public static final String ERR_3013_MSG = "서버아이피 인증일 경우는 아이디가 필수입니다.";

	public static final String ERR_4000 = "4000";
	public static final String ERR_4000_MSG = "라이센스 에러";

	public static final String ERR_4001 = "4001";
	public static final String ERR_4001_MSG = "해댱 앱아이디는 라이센스가 없습니다.";

	public static final String ERR_4002 = "4002";
	public static final String ERR_4002_MSG = "라이센스 정보가 올바르지 않습니다.";

	public static final String ERR_5000 = "5000";
	public static final String ERR_5000_MSG = "푸시서비스 가입 에러.";

}
