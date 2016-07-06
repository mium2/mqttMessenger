package com.msp.messenger.util.security;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.BadPaddingException;

import com.msp.messenger.util.AES;
import com.msp.messenger.util.SEED;
import com.msp.messenger.util.security.exception.InvalidExpireTimeException;
import com.msp.messenger.util.security.exception.InvalidRemoteServerException;
import com.msp.messenger.util.security.exception.InvalidUserException;
import com.msp.messenger.util.security.exception.NonExistAuthorizationKeyException;
import com.msp.messenger.util.security.exception.SecurityAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityAuthorization {

	private static SecurityAuthorization instance = null;

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	final private int devisionForModVal_alg = 3;
	final private int devisionForModVal_key = 7;

	final private String expireTimeFormat = "yyyyMMddHHmmssSSS";

	private Properties myProperties = null;

	private int intervalMinute = 0;

	// A:AES, S:SEED
	private char[] securityAlgorithm = {'A', 'S'};
	private Hashtable<Integer,String> securityKeys = null;

	private Hashtable<String,String> ignoreTargets = null;


	private SecurityAuthorization(Properties myProperties) {
		this.myProperties = myProperties;
		this.ignoreTargets = new Hashtable<String,String>();
		this.securityKeys = new Hashtable<Integer,String>();
		setInit();
	}

	public static SecurityAuthorization getInstance(Properties myProperties){
		if(instance==null){
			instance = new SecurityAuthorization(myProperties);
		}
		return instance;
	}

	private void setInit(){
		// 인증키 갱신 주기 세팅 (단위 : 분)
		intervalMinute = Integer.parseInt(myProperties.getProperty("token.expire.minute","30").trim());

		// 인증키를 사용하지 않을 미리 세팅된 Legacy Server IP 세팅
		int ignoreTargetCount = Integer.parseInt(myProperties.getProperty("security.ignore.targetCount").trim());
		for(int i=0; i<ignoreTargetCount; i++){
			try{
				String org_setIP = myProperties.getProperty("security.ignore.targets.ip" + (i+1)).trim();
				if(org_setIP.indexOf(".")>-1){
					String[] settingIPClass = org_setIP.split("\\.");
					if(settingIPClass.length==4){
						String ABC_classIP = settingIPClass[0]+"."+settingIPClass[1]+"."+settingIPClass[2]+".";
						String D_classIP = settingIPClass[3];
						if(D_classIP.indexOf("*")>-1){
							for(int j=0; j<=255; j++){
								String setIP = ABC_classIP+j;
								ignoreTargets.put(setIP,setIP);
							}
							continue;
						}else if(D_classIP.indexOf("-")>-1){
							String[] gapIP = D_classIP.split("\\-");
							int startIP = Integer.parseInt(gapIP[0].trim());
							int endIP = Integer.parseInt(gapIP[1].trim());
							if(startIP>endIP){
								for(int j=endIP; j<=startIP; j++){
									String setIP = ABC_classIP+j;
									ignoreTargets.put(setIP,setIP);
								}
							}else if(startIP==endIP){
								String setIP = ABC_classIP+startIP;
								ignoreTargets.put(setIP,setIP);
							}else if(startIP<endIP){
								for(int j=startIP; j<=endIP; j++){
									String setIP = ABC_classIP+j;
									ignoreTargets.put(setIP,setIP);
								}
							}
							continue;
						}
					}
				}

				ignoreTargets.put(org_setIP,org_setIP);
			}catch(NullPointerException e){
				logger.info("관리자가 세팅한 \"targetCount\" 보다 실제 ip1...ip[n] 개수가 적습니다. " + (i) + "개수까지만 서버에 적용합니다");
				break;
			}
		}

		logger.info("#################################################################");
		logger.info("#########         SETTING IP                           ##########");
		logger.info("#################################################################");

		Set<Map.Entry<String,String>> entrySet = ignoreTargets.entrySet();
		for(Map.Entry<String,String> mapEntry : entrySet){
			logger.info("# Registration IP :" + mapEntry.getKey());
		}
		logger.info("#################################################################");
	}

	public boolean isCheckAuthIP(String targetIP) throws Exception{
		if(ignoreTargets.containsKey(targetIP.trim())){
			logger.debug("외부 서버로부터 정상적인 접근입니다. [접속 IP : " + targetIP.trim() + "]");
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 인증키를 사용하지 않을 미리 세팅된 Legacy Server IP 검증
	 * @param targetIp
	 * @return
	 * @throws Exception
	 */
	public boolean isAuthenticationVerification(String cuid, String psid, String authorizationKey, String targetIp, String reqUri) throws Exception{
		if((cuid == null || cuid.trim().equals("")) && (psid == null || psid.trim().equals(""))){
			if(targetIp == null || targetIp.trim().equals("")){
				throw new InvalidRemoteServerException("잘못된 접근 시도입니다. [알수 없는 접속 IP is NULL]");
			}else{
				if(ignoreTargets.containsKey(targetIp.trim())){
					logger.debug("외부 서버로부터 정상적인 접근입니다. [접속 IP : " + targetIp.trim() + "]");
					return true;
				}else{
					throw new InvalidRemoteServerException("비인가 서버로부터 잘못된 접근 시도입니다. [비인가 접속 IP : " + targetIp.trim() + "]");
				}
			}
		}else{
			if(ignoreTargets.containsKey(targetIp.trim())){
				logger.debug("외부 서버로부터 정상적인 접근입니다. [접속 IP : " + targetIp.trim() + "]");
				return true;
			}

			/**
			 * 외부서버에서 접근 시 CUID가 존재하는 API에 대해서는 isCertificationPass() 를 탈 수밖에 없다.
			 * 단말도 서버와 동일하게 IP가 존재하기 때문이다. 단, 단말을 서버와 틀리게 모든 API에 대해서 CUID를 올려야 하기 때문
			 * 단 본 조건에서 비정삭적인 서버 접근 시 new NonExistAuthorizationKeyException("보안인증키가 없습니다. 신규 키를 발급 받아야 합니다."); 가 발생되며,
			 * 보안 인증키가 있어도 정상적이지 않으면 실패 처리 된다.
			 */

			return isCertificationPass(cuid, psid, authorizationKey, reqUri);
		}
	}

	/**
	 * 인증 보안키 생성 및 반환
	 * @param cuid
	 * @return
	 * @throws Exception
	 */
	public String getSecurityAuthorizationKey(String cuid, String psid) throws Exception{
		if(psid != null && !psid.trim().equals("")){
			psid = makeAuthSeedKey(psid);
			return excuteEncryption(psid, getMySecurityAlgorithmNumber(), getMySecurityKeyNumber(), psid + "|" + getExpireTime());
		}else{ //임시키 발급은 유효시간을 1분으로 짧게 준다.
			return "C=" + excuteEncryption(cuid, getMySecurityAlgorithmNumber(), getMySecurityKeyNumber(), cuid + "|" + getTmpKeyExpireTime());
		}
	}

	/**
	 * 인증 보안키 검증
	 * @param cuid
	 * @param authorizationKey
	 * @return
	 * @throws Exception
	 */
	private boolean isCertificationPass(String cuid, String psid, String authorizationKey, String reqUri) throws Exception{
		Hashtable<String, Object> decryptionKeyValues = null;

		boolean isCuidEncript = false;

		if(authorizationKey == null || authorizationKey.trim().equals("")){
			throw new NonExistAuthorizationKeyException("보안인증키가 없습니다. 신규 키를 발급 받아야 합니다.");
		}else{
			/**
			 * 퍼블릭 푸쉬 서비스 등록은 파라미터로 PSID가 올라 오지만 DB에 없기 때문에 이전에 내려간
			 * 보안키가 CUID로 말렸을 것을 대비하여 CUID로 복호화할 수 있도록 처리하는 로직
			 */
			if(isCuidEncript = authorizationKey.subSequence(0,2).equals("C=")){
				authorizationKey = authorizationKey.substring(2);
				logger.debug("======> authorizationKey \"C=\" Prefix 제거 : " + authorizationKey);
			}
		}

		try {
			/**
			 * 퍼블릭 푸쉬 서비스 등록은 파라미터로 PSID가 올라 오지만 DB에 없기 때문에 이전에 내려간
			 * 보안키가 CUID로 말렸을 것을 대비하여 CUID로 복호화할 수 있도록 처리하는 로직
			 */
			if(isCuidEncript){
				decryptionKeyValues = excuteDecryption(cuid, authorizationKey);
				if(!cuid.equals(decryptionKeyValues.get("baseid"))){
					logger.debug("======> cuid : " + cuid);
					logger.debug("======> baseid : " + decryptionKeyValues.get("baseid"));
					throw new InvalidUserException("파라미터의 사용자 아이디와 보안인증키 내의 사용자 아이디가 일치하지 않습니다.");
				}
			}else{
				psid = makeAuthSeedKey(psid);
				decryptionKeyValues = excuteDecryption(psid, authorizationKey);
				logger.info("#############@@@@@@ decryptionKeyValues.get(baseid) :"+decryptionKeyValues);
				if(!psid.equals(decryptionKeyValues.get("baseid"))){
					logger.debug("======> psid : " + psid);
					logger.debug("======> baseid : " + decryptionKeyValues.get("baseid"));
					throw new InvalidUserException("파라미터의 사용자 아이디와 보안인증키 내의 사용자 아이디가 일치하지 않습니다.");
				}
			}

			if(!isSomeFreeTime((Calendar)decryptionKeyValues.get("expireTime"))){
				throw new InvalidExpireTimeException("접근 유효시간이 만료되었습니다. 새로 키를 발급 받아야 합니다.");
			}

			logger.debug("정상적인 접근 유효시간을 갖은 단말로부터 접근입니다.");

			return true;
		} catch (NumberFormatException e) {
			// 암호화된 보안토근 내의 CUID와 넘겨받은 CUID 길이가 틀려서 발생
			throw new SecurityAuthorizationException("잘못된 인증키 입니다.\n-암호화된 보안토근 내의 CUID와 넘겨받은 CUID 길이가 틀려서 발생");
		} catch (BadPaddingException e) {
			// 암호키가 틀려 복호화 실패 시 발생 (SEED 복호화 요청시 발생)
			// 암호화 알고리즘이 틀린경우 발생 (AES 복호화 요청시 발생)
			throw new SecurityAuthorizationException("잘못된 인증키 입니다.\n-암호키가 틀려 복호화 실패 시 발생 (SEED 복호화 요청시 발생)\n-암호화 알고리즘이 틀린경우 발생 (AES 복호화 요청시 발생)");
		} catch (StringIndexOutOfBoundsException e) {
			// 암호화 알고리즘이 틀린경우 발생 (SEED 복호화 요청시 발생)
			// 암호키가 틀려 복호화 실패 시 발생 (AES 복호화 요청시 발생)
			throw new SecurityAuthorizationException("잘못된 인증키 입니다.\n-암호화 알고리즘이 틀린경우 발생 (SEED 복호화 요청시 발생)\n-암호키가 틀려 복호화 실패 시 발생 (AES 복호화 요청시 발생)");
		} catch (ArrayIndexOutOfBoundsException e){
			// 잘못된 인증키로 인해 파싱한 알고리즘 index나 키 index 위치가 틀린경우 발생
			throw new SecurityAuthorizationException("잘못된 인증키 입니다.\n-잘못된 인증키로 인해 파싱한 알고리즘 index나 키 index 위치가 틀린경우 발생");
		} catch (Exception e){
			// 잘못된 인증키로 인해 파싱한 알고리즘 index나 키 index 위치가 틀린경우 발생
			throw new SecurityAuthorizationException("잘못된 인증키 입니다.\n-잘못된 인증키로 인해 기타 발생");
		} finally {
			logger.debug("# cuid = " + cuid);
			logger.debug("# psid = " + psid);
			logger.debug("# authorizationKey = " + authorizationKey);
		}
	}

	private int getMySecurityAlgorithmNumber(){
		return getRandomNumber(securityAlgorithm.length);
	}

	private int getMySecurityKeyNumber(){
		return getRandomNumber(securityKeys.size());
	}

	private int getRandomNumber(int range){
		return new Random().nextInt(range);
	}

	private String getExpireTime(){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, intervalMinute);
//		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + intervalMinute);

		return new SimpleDateFormat(expireTimeFormat).format(cal.getTime());
	}

	private String getTmpKeyExpireTime(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + 1);

		return new SimpleDateFormat(expireTimeFormat).format(cal.getTime());
	}

	private boolean isSomeFreeTime(Calendar expireTime){
		Calendar cal = Calendar.getInstance();

		logger.info("현재기준시간 [" + new SimpleDateFormat(expireTimeFormat).format(cal.getTime()) + "]");
		logger.info("인증만료시간 [" + new SimpleDateFormat(expireTimeFormat).format(expireTime.getTime()) + "]");

		if(cal.getTimeInMillis() <= expireTime.getTimeInMillis()){
			return true;
		}else{
			return false;
		}
	}

	private String excuteEncryption(String baseId, int algNum, int keyNum, String targetStr) throws Exception{
		logger.debug("Encryption - baseId = " + baseId);
		logger.debug("Encryption - baseId.length() = " + baseId.length());
		logger.debug("Encryption - algNum = " + algNum);
		logger.debug("Encryption - keyNum = " + keyNum);
		logger.debug("Encryption - targetStr = " + targetStr);

		String authorizationKey = null;
		switch(securityAlgorithm[algNum]){
			case 'A': authorizationKey = encryptionByAES(keyNum, targetStr); break;
			case 'S': authorizationKey = encryptionBySEED(keyNum, targetStr); break;
		}

		logger.debug("Encryption - authorizationKey 1 = " + authorizationKey);

		int algTrickLocation = baseId.trim().length() % devisionForModVal_alg;
		int keyTrickLocation = baseId.trim().length() % devisionForModVal_key;
		int keyNumLength = (keyNum+"").length();

		logger.debug("Encryption - algTrickLocation = " + algTrickLocation);
		logger.debug("Encryption - keyTrickLocation = " + keyTrickLocation);
		logger.debug("Encryption - keyNumLength = " + keyNumLength);

		authorizationKey = authorizationKey.substring(0, algTrickLocation) + algNum + authorizationKey.substring(algTrickLocation);

		logger.debug("Encryption - authorizationKey 2 = " + authorizationKey);

		authorizationKey = authorizationKey.substring(0, keyTrickLocation) + keyNumLength + keyNum + authorizationKey.substring(keyTrickLocation);

		logger.debug("Encryption - authorizationKey 3 = " + authorizationKey);

		return authorizationKey;
	}

	private String encryptionByAES(int keyNum, String targetStr) throws Exception{
		return AES.encryptToBase64(targetStr.getBytes("UTF-8"), securityKeys.get(keyNum));
	}

	private String encryptionBySEED(int keyNum, String targetStr) throws Exception{
		String encrytionKey = securityKeys.get(keyNum);
		logger.debug("######  securityKeys.get(keyNum):"+encrytionKey);
		return SEED.encryptToBase64(targetStr.getBytes("UTF-8"), encrytionKey);
	}

	private Hashtable<String, Object> excuteDecryption(String baseId, String authorizationKey) throws NumberFormatException, Exception{
		logger.debug("Decryption - baseId = " + baseId);
		logger.debug("Decryption - authorizationKey = " + authorizationKey);
		logger.debug("======> baseId.trim().length() = " + baseId.trim().length());
		logger.debug("======> devisionForModVal_alg = " + devisionForModVal_alg);
		logger.debug("======> devisionForModVal_key = " + devisionForModVal_key);

		int algTrickLocation = baseId.trim().length() % devisionForModVal_alg;
		int keyTrickLocation = baseId.trim().length() % devisionForModVal_key;

		logger.debug("Decryption - algTrickLocation = " + algTrickLocation);
		logger.debug("Decryption - keyTrickLocation = " + keyTrickLocation);

		int keyLeng = Integer.parseInt(authorizationKey.substring(keyTrickLocation, keyTrickLocation + 1));
		int keyNum = Integer.parseInt(authorizationKey.substring(keyTrickLocation + 1, keyTrickLocation + 1 + keyLeng));

		logger.debug("Decryption - keyLeng = " + keyLeng);
		logger.debug("Decryption - keyNum = " + keyNum);

		authorizationKey = authorizationKey.substring(0, keyTrickLocation) + authorizationKey.substring(keyTrickLocation + 1 + keyLeng);

		logger.debug("Decryption - authorizationKey 2 = " + authorizationKey);

		int algNum = Integer.parseInt(authorizationKey.substring(algTrickLocation, algTrickLocation + 1));

		authorizationKey = authorizationKey.substring(0, algTrickLocation) + authorizationKey.substring(algTrickLocation + 1);

		logger.debug("Decryption - authorizationKey 2 = " + authorizationKey);

		logger.debug("Decryption - algNum = " + algNum);

		return getDecryptionKeyValues(algNum, keyNum, authorizationKey);
	}

	private Hashtable<String, Object> getDecryptionKeyValues(int algNum, int keyNum, String authorizationKey) throws Exception{
		String decryptionKey = null;
		try {
			//빈공간이 생겼을때 에러가 발생한다.
			authorizationKey = authorizationKey.replace(" ","+");
			switch (securityAlgorithm[algNum]) {
				case 'A':
					decryptionKey = decryptionByAES(keyNum, authorizationKey);
					break;
				case 'S':
					decryptionKey = decryptionBySEED(keyNum, authorizationKey);
					break;
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		String baseid = decryptionKey.substring(0, decryptionKey.indexOf("|"));
		String expireTime = decryptionKey.substring(decryptionKey.indexOf("|") + 1);

		Calendar calExpireTime = Calendar.getInstance();
		calExpireTime.setTime(new SimpleDateFormat(expireTimeFormat).parse(expireTime));

		Hashtable<String, Object> decryptionKeyValues = new Hashtable<String, Object>();
		decryptionKeyValues.put("baseid", baseid);
		decryptionKeyValues.put("expireTime", calExpireTime);

		return decryptionKeyValues;
	}

	private String decryptionByAES(int keyNum, String authorizationKey) throws Exception{
		return new String(AES.decryptFromBase64(authorizationKey, securityKeys.get(keyNum)));
	}

	private String decryptionBySEED(int keyNum, String authorizationKey) throws Exception{
		return new String(SEED.decryptFromBase64(authorizationKey, securityKeys.get(keyNum)));
	}

	private String makeAuthSeedKey(String psid){
		if(psid.length()>15){
			psid = psid.substring(2,12);
		}
		return psid;
	}
}





