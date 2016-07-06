/**
 * 
 */
package com.msp.messenger.util;

/**
 * 
 * <code>
 * 	static String plaintext = "324234fsg Hello, World! fgd345345 345dfg ABCDEFGHIJKLMNOPQRSTUVWXYZ"; // Note: PKCS5Padding
 *	static String encryptionKey = "0123456789abcdegasfd3 34534";  // Note: any length
 *
 *	public static void main(String [] args) {
 *		try {
 *			System.out.println("== AES test ==");
 *			System.out.println("plain:\t" + plaintext);
 *			String cipherB64 = encryptToBase64(plaintext.getBytes("UTF-8"), encryptionKey);
 *			System.out.println("cipher:\t" + cipherB64);
 *			byte [] decryptB64 = decryptFromBase64(cipherB64, encryptionKey);
 *			System.out.println("decrypt:\t" + new String(decryptB64, "UTF-8"));
 *		} catch (Exception e) {
 *			e.printStackTrace();
 *		} 
 *	}
 * </code>
 *
 * @author cronosalt
 *
 */

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	static byte[] iv ={
		(byte)0x00,(byte)0xaa,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x55,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
	static IvParameterSpec ips = new IvParameterSpec(iv);

	// encryption key를 16바이트(256bit)로 padding 하기 위해 MD5 hash 사용
	private static byte [] paddingKey(String key) throws Exception {
		MessageDigest md= MessageDigest.getInstance("MD5");
		md.update(key.getBytes("UTF-8"));
		return md.digest();
	}
	
	// byte [] -- encrypt --> byte []
	/**
	 * 바이트 배열 데이터를 encryptionKey를 사용해 AES 암호화 한 후 결과 바이트 배열 결과를 리턴한다.
	 * 
	 * @param plainData 평문 데이터
	 * @param encryptionKey 암호화 키로 사용할 임의의 길이 문자열
	 * @return 암호화된 데이터
	 * @throws Exception
	 */
	public static byte [] encrypt(byte [] plainData, String encryptionKey) throws Exception {
		byte [] paddedKey = paddingKey(encryptionKey);

	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
	    SecretKeySpec key = new SecretKeySpec(paddedKey, "AES");
	    cipher.init(Cipher.ENCRYPT_MODE, key, ips);

	    return cipher.doFinal(plainData);
	}

	// byte [] -- encrypt --> String(base64 encoded)
	/**
	 * 바이트 배열 데이터를 encryptionKey를 사용해 AES 암호화 한 후 결과를 BASE64 인코딩하여 리턴한다.
	 * 
	 * @param plainData 평문 데이터
	 * @param encryptionKey 암호화 키로 사용할 임의의 길이 문자열
	 * @return 암호화된 데이터의 base64 문자열
	 * @throws Exception
	 */
	public static String encryptToBase64(byte [] plainData, String encryptionKey) throws Exception {
	    return com.msp.messenger.util.Base64.encodeToString(encrypt(plainData, encryptionKey), true); // according to RFC 2045
	}

	// byte [] -- decrypt --> byte []
	/**
	 * 암호화된 바이트 배열 데이터를 encryptionKey를 사용해 AES 복호화 한 후 결과 바이트 배열을 리턴한다.
	 *
	 * @param cipherText 암호화된 데이터
	 * @param encryptionKey 복호화 키로 사용할 임의의 길이 문자열
	 * @return 평문 데이터
	 * @throws Exception
	 */
	public static byte [] decrypt(byte [] cipherText, String encryptionKey) throws Exception {
        System.out.println("###### cipherText :"+cipherText +"  encryptionKey: "+encryptionKey);
		byte [] paddedKey = paddingKey(encryptionKey);

	    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
	    SecretKeySpec key = new SecretKeySpec(paddedKey, "AES");
	    cipher.init(Cipher.DECRYPT_MODE, key, ips);

		return cipher.doFinal(cipherText);
	}

	// String(base64 encoded) -- decrypt --> byte []
	/**
	 * 암호화된 바이트 배열 데이터의 BASE64 형태 포맷 문자열을 encryptionKey를 사용해 AES 복호화 한 후 결과 바이트 배열을 리턴한다.
	 *
	 * @param cipherText 암호화된 base64 문자열
	 * @param encryptionKey 복호화 키로 사용할 임의의 길이 문자열
	 * @return 평문 데이터
	 * @throws Exception
	 */
	public static byte [] decryptFromBase64(String cipherText, String encryptionKey) throws Exception {
        System.out.println("######### cipherText:"+cipherText+"   encryptionKey: "+encryptionKey);
	    return decrypt(Base64.decode(cipherText), encryptionKey);
	}
}
