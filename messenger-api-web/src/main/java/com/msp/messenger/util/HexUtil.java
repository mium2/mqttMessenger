package com.msp.messenger.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.msp.messenger.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HexUtil {
	private static Logger logger = LoggerFactory.getLogger("com.msp.messenger.util.HexUtil.class");
	
	private static String getHash(String msg, String algorithm) {
		if (msg == null) {
			return "";
		}
		byte[] defaultBytes = msg.getBytes();
		StringBuffer hexString = new StringBuffer();
		try{
			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.reset();
			md.update(defaultBytes);
			byte messageDigest[] = md.digest();

			hexString.append(byteToHex(messageDigest));
//			logger.debug("message " + msg + " " + algorithm + " version is " + hexString.toString());
		} catch (NoSuchAlgorithmException nsae){
//			nsae.printStackTrace();
			logger.error("", nsae);
		    hexString.append(msg);
		}
		return hexString.toString();
	}

	/**
	 * MD5 hash value를 구한다. (32 chars)
	 * 
	 * @param msg
	 * @return
	 */
	public static String getMD5(final String msg) {
		return getHash(msg, "MD5");
	}
	
	/**
	 * SHA-1 hash value를 구한다. (40 chars)
	 * 
	 * @param msg
	 * @return
	 */
	public static String getSHA1(final String msg) {
		return getHash(msg, "SHA-1");
	}
	
	private static String byteToHex(final byte[] hash) {
	    Formatter formatter = new Formatter();
	    for (byte b : hash) {
	        formatter.format("%02x", b);
	    }
	    String result = formatter.toString();
	    formatter.close();
	    return result;
	}

	/**
	 * reqMap에서 hash값 구할 대상을 찾아 hash를 구해 paramMap에 넣는다.
	 * 
	 * @param paramMap
	 * @param reqMap
	 */
	public static void putHashForMap(Map<String, Object> paramMap, Map<String, Object> reqMap) {
		// Hash값을 구해야 하는 파라미터에 대해 MD5 해시 적용
		for (Iterator<String> iterator = reqMap.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			if (key.length() > 3 && key.endsWith(Constants.PARAM_HASH_SUFFIX)) {
				Object value = reqMap.get(key);
				String newKey = key.substring(0, key.length() - Constants.PARAM_HASH_SUFFIX.length()) + Constants.PARAM_MD5_SUFFIX;
				String newValue = null;
				if (value instanceof String[]) {
					String[] valueList = (String[])value;
					if (valueList[0].length() != 0) {
						newValue = HexUtil.getMD5(valueList[0]);
					}
				} else {
					if (((String)value).length() != 0) {
						newValue = HexUtil.getMD5((String) value);
					}
				}
				if (newValue != null) {
					paramMap.put(newKey, newValue);
				}
			}
		}
	}
	
	/**
	 * messageUniqueKey를 생성하여 리턴한다.
	 * 메세지키를 기존 cuid, psid를 이용한경우도 같은 키가 나올 수 있는 확률이 있어 UUID로 변경함
	 * @return
	 */
	public static String getMessageUniqueKey(String DB_IN) {
        String strUniqueKey = UUID.randomUUID().toString();
        strUniqueKey = strUniqueKey.replaceAll("-","");
        if(DB_IN!=null && DB_IN.equals("Y")){
            strUniqueKey = "I_"+strUniqueKey;
        }
		return strUniqueKey;
	}
}
