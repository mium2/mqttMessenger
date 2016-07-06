package com.msp.messenger.util;

import java.security.MessageDigest;

public class SEED {
	static byte[] iv ={
		(byte)0x00,(byte)0xaa,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
		(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x55,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

	// encryption key를 16바이트(256bit)로 padding 하기 위해 MD5 hash 사용
	private static byte [] paddingKey(String key) throws Exception {
		MessageDigest md= MessageDigest.getInstance("MD5");
		md.update(key.getBytes("UTF-8"));
		return md.digest();
	}
	
	/**
	 * 바이트 배열 데이터를 encryptionKey를 사용해 SEED 암호화 한 후 결과 바이트 배열 결과를 리턴한다.
	 * 
	 * @param plainData 평문 데이터
	 * @param encryptionKey 암호화 키로 사용할 임의의 길이 문자열
	 * @return 암호화된 데이터
	 * @throws Exception
	 */
	public static byte [] encrypt(byte [] plainData, String encryptionKey) throws Exception {
		byte [] keyBytes = paddingKey(encryptionKey);
		int[] seedKey = new int[32];
		SEED_KISA.SeedRoundKey(seedKey, keyBytes);

		return SEEDUtil.getSeedEncrypt(plainData, seedKey);
	}

	/**
	 * 바이트 배열 데이터를 encryptionKey를 사용해 SEED 암호화 한 후 결과를 BASE64 인코딩하여 리턴한다.
	 *
	 * @param plainData 평문 데이터
	 * @param encryptionKey 암호화 키로 사용할 임의의 길이 문자열
	 * @return 암호화된 데이터의 base64 문자열
	 * @throws Exception
	 */
	public static String encryptToBase64(byte [] plainData, String encryptionKey) throws Exception {
		return Base64.encodeToString(encrypt(plainData, encryptionKey), true); // according to RFC 2045
	}

	/**
	 * 암호화된 바이트 배열 데이터를 encryptionKey를 사용해 SEED 복호화 한 후 결과 바이트 배열을 리턴한다.
	 *
	 * @param cipherText 암호화된 데이터
	 * @param encryptionKey 복호화 키로 사용할 임의의 길이 문자열
	 * @return 평문 데이터
	 * @throws Exception
	 */
	public static byte [] decrypt(byte [] cipherText, String encryptionKey) throws Exception {
		byte [] keyBytes = paddingKey(encryptionKey);
		int[] seedKey = new int[32];
		SEED_KISA.SeedRoundKey(seedKey, keyBytes);

		return SEEDUtil.getSeedDecrypt(cipherText, seedKey);
	}
	
	/**
	 * 암호화된 바이트 배열 데이터의 BASE64 형태 포맷 문자열을 encryptionKey를 사용해 SEED 복호화 한 후 결과 바이트 배열을 리턴한다.
	 * 
	 * @param cipherText 암호화된 base64 문자열
	 * @param encryptionKey 복호화 키로 사용할 임의의 길이 문자열
	 * @return 평문 데이터
	 * @throws Exception
	 */
	public static byte [] decryptFromBase64(String cipherText, String encryptionKey) throws Exception {
		return decrypt(Base64.decode(cipherText), encryptionKey);
	}
	

}
