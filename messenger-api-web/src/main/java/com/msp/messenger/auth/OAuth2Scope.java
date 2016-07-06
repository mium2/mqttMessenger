package com.msp.messenger.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Description : 인증서버, 리소스서버가 공통으로 사용할 수 있는 scope 정의/검증 클래스
 * Created by mium2(Y.B.H) 2015-02-12 오후 1:04
 */
@Component
public class OAuth2Scope {
    private static Logger logger = LoggerFactory.getLogger("com.msp.messenger.auth.OAuth2Scope.class");
	// Scope 상수 등록
	//OAuth2Scope : 이것은 조직의 성격에 맞게 수정한다.
	public static final String ROLE_SUPER_ADMIN = "SA";
	public static final String ROLE_SERVICE_MASTER = "SM";
	public static final String ROLE_SERVICE_PROVIDER = "SP";
	public static final String ROLE_SERVICE_CLIENT = "SC";
	public static final String ROLE_SERVICE_GUEST = "SG";

	// 각 REST 엔드포인트마다 허용할 scope 지정
	private static HashMap<String, String> scopeUrlMap;
	// Client 등록 화면에 보여줄 scope 지정
	public static TreeMap<String,String> scopeMsgMap;
	
	static {
		scopeUrlMap = new HashMap<String, String>();
		scopeMsgMap = new TreeMap<String, String>();
		
		// 조직의 상황에 맞게 이곳에 각 엔드포인트와 scope 을 등록한다.
		// 해당 엔드포인트에 대해서는 지정된 scope이 있어야만 접근 가능하다.
		// 엔드포인트는 ContextPath는 제거하고 입력한다.
		scopeUrlMap.put("POST /checkUserAndRegist", ROLE_SERVICE_MASTER);



		//초기화 이것의 각 조직의 scope 에 맞게 등록한다.
		//클라이언트 앱 등록시 보여질 화면에도 사용됨.
		scopeMsgMap.put(ROLE_SUPER_ADMIN, "관리자 API를 사용 할 수 있습니다.");
		scopeMsgMap.put(ROLE_SERVICE_MASTER, "서비스 마스터 API를 사용 할 수 있습니다.");
		scopeMsgMap.put(ROLE_SERVICE_CLIENT, "서비스 클라이언트 API를 사용 할 수 있습니다.");
		scopeMsgMap.put(ROLE_SERVICE_GUEST, "GUEST API를 사용 할 수 있습니다.");
	}
	
	public static String getScopeFromURI(String uri) {
		return scopeUrlMap.get(uri);
	}

	public static String getScopeMsg(String scopeKey) {
		return scopeMsgMap.get(scopeKey);
	}
	
	public static boolean isScopeExistInMap(String strScope) {
		boolean isValid = true;
		String[] scopes = strScope.split(",");
		for (int i=0; i < scopes.length; i++) {
			String v = getScopeMsg(scopes[i]);
			if (v == null) {
				isValid = false; break;
			}
		}
		return isValid;
	}
	
	public static boolean isScopeValid(String receivedScope, String registeredClientScope) {
        logger.debug("[Scope Compare] : "+receivedScope+"   "+registeredClientScope);
		String rscopes[] = receivedScope.split(",");
		String temp[] = registeredClientScope.split(",");
		
		List<String> sscopes = Arrays.asList(temp);
		//System.out.println(sscopes);
		boolean isValid = true;
		for (int i=0; i < rscopes.length; i++) {
			if (sscopes.contains(rscopes[i]) == false) {
				isValid = false;
				break;
			}
		}
		return isValid;
	}

	public static boolean isUriScopeValid(String uriScope, String tokenScopes) {
		String temp[] = tokenScopes.split(",");
		List<String> sscopes = Arrays.asList(temp);
		if (sscopes.contains(uriScope))
			return true;
		else
			return false;
	}
}
