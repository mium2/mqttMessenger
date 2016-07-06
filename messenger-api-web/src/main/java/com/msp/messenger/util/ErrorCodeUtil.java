package com.msp.messenger.util;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.msp.messenger.common.exception.SenderNotExistException;
import com.msp.messenger.common.exception.UnauthorizedException;
import com.msp.messenger.util.security.exception.InvalidExpireTimeException;
import com.msp.messenger.util.security.exception.InvalidRemoteServerException;
import com.msp.messenger.util.security.exception.InvalidUserException;
import com.msp.messenger.util.security.exception.NonExistAuthorizationKeyException;
import com.msp.messenger.util.security.exception.SecurityAuthorizationException;

import org.springframework.dao.DataIntegrityViolationException;

public class ErrorCodeUtil {
	public final static Map<String, Object> errorCodes = ImmutableMap(
			InvalidExpireTimeException.class.getName(),			"-500",		"Invalid ExpireTime  (정상적인 호출이나 인증키 사용시간 만료)",
			IllegalArgumentException.class.getName(),			"-400",		"Illegal Argument",
			SenderNotExistException.class.getName(),			"-300",		"Sender Not Exist",
			UnauthorizedException.class.getName(),				"-200",		"UNAUTHORIZED",
			SQLException.class.getName(),						"-100",		"DB ERROR",
			DataIntegrityViolationException.class.getName(),	"-101",		"SQL ERROR(DataIntegrityViolationException)",
			DataIntegrityViolationException.class.getName(),	"-102",		"SQL ERROR(BadSqlGrammarException)",
			Exception.class.getName(),							"-1",		"SYSTEM ERROR"
		);

	public final static Map<String, Object> mobileErrorCodes = ImmutableMap(
			// 보안인증에서 Json type으로 내려가는 에러 코드는 업무로직 에러코드와 구분하기 위해 00을 Tail을 붙힌다.
			NonExistAuthorizationKeyException.class.getName(),	"40000",	"Bad request (클라이언트 잘못된 요청으로 처리 못함)",
			InvalidUserException.class.getName(),				"40300",	"Forbidden (존재하지 않는 사용자이거나 접근이 거부되었습니다.)",
			SecurityAuthorizationException.class.getName(),		"40100",	"Unauthorized (인증이 실패되었습니다.)",
			InvalidRemoteServerException.class.getName(),		"50300",	"Bad request (허가 안된 외부서버 또는 클라이언트로부터의 잘못된 요청입니다.)",
			
			UnauthorizedException.class.getName(),				"401",		"UNAUTHORIZED",
			SQLException.class.getName(),						"500",		"DB ERROR",
			DataIntegrityViolationException.class.getName(),	"500",		"SQL ERROR(DataIntegrityViolationException)",
			DataIntegrityViolationException.class.getName(),	"500",		"SQL ERROR(BadSqlGrammarException)",
			Exception.class.getName(),							"500",		"SYSTEM ERROR"
		);

	@SuppressWarnings("unchecked")
	public static String getErrorCode(Exception e) {
		Map<String, String> values = (Map<String, String>)errorCodes.get(e.getClass().getName());
		String errorCode = "-1";
		if (values == null) {
			values = (Map<String, String>)errorCodes.get(Exception.class.getName());
			errorCode = values.get("CODE");
		} else {
			errorCode = values.get("CODE");
		}
		return errorCode;
	}
	
	@SuppressWarnings("unchecked")
	public static String getErrorMessage(Exception e) {
		Map<String, String> values = (Map<String, String>)errorCodes.get(e.getClass().getName());
		String errorMessage = "SYSTEM ERROR";
		if (values == null) {
			values = (Map<String, String>)errorCodes.get(Exception.class.getName());
			errorMessage = values.get("MESSAGE");
		} else {
			errorMessage = values.get("MESSAGE");
		}
		return errorMessage;
	}

	@SuppressWarnings("unchecked")
	public static String getMobileErrorCode(Exception e) {
		Map<String, String> values = (Map<String, String>)mobileErrorCodes.get(e.getClass().getName());
		String errorCode = "-1";
		if (values == null) {
			values = (Map<String, String>)mobileErrorCodes.get(Exception.class.getName());
			errorCode = values.get("CODE");
		} else {
			errorCode = values.get("CODE");
		}
		return errorCode;
	}
	
	@SuppressWarnings("unchecked")
	public static String getMobileErrorMessage(Exception e) {
		Map<String, String> values = (Map<String, String>)mobileErrorCodes.get(e.getClass().getName());
		String errorMessage = "SYSTEM ERROR";
		if (values == null) {
			values = (Map<String, String>)mobileErrorCodes.get(Exception.class.getName());
			errorMessage = values.get("MESSAGE");
		} else {
			errorMessage = values.get("MESSAGE");
		}
		return errorMessage;
	}

	@SuppressWarnings("unchecked")
	public static <K,V> Map<K,V> ImmutableMap(Object... keyValPair){
	    Map<K,V> map = new HashMap<K,V>();

	    if(keyValPair.length % 3 != 0){
	        throw new IllegalArgumentException("Keys and values must have 3 components.");
	    }

	    for(int i = 0; i < keyValPair.length; i += 3){
	    	HashMap<String, String> values = new HashMap<String, String>();
	    	values.put("CODE", (String)keyValPair[i+1]);
	    	values.put("MESSAGE", (String)keyValPair[i+2]);
	        map.put((K) keyValPair[i], (V) values);
	    }

	    return Collections.unmodifiableMap(map);
	}
}
