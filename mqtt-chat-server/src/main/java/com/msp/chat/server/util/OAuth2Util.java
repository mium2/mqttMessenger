package com.msp.chat.server.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SuppressWarnings("deprecation")
public class OAuth2Util {
    // URL Encoding Utility
    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!").replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static String decodeURIComponent(String s) {
        if (s == null) {
            return null;
        }
        String result = null;
        try {
            result = URLDecoder.decode(s, "UTF-8");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

    public static byte[] hexToBinary(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer
                    .parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    // byte[] to hex
    public static String binaryToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

    public static String getHmacSha256(String str) {
        byte[] binary = null;
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes("UTF-8"));
            binary = sh.digest();
        }catch(Exception e){
            e.printStackTrace();
        }

        return binaryToHex(binary);
    }


    // Access Token과 refresh_token을 랜덤하게 생성함.
    public static String generateToken() {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte ramdomBytes[] = new byte[256];
            secureRandom.nextBytes(ramdomBytes);
            secureRandom.setSeed(ramdomBytes);
//            secureRandom.setSeed(secureRandom.generateSeed(256));  //리눅스 버전 jdk에서 hang이 걸리는 경우가 있음.
            MessageDigest digest = MessageDigest.getInstance("SHA-1"); //MD5,SHA-1,SHA-256
            byte[] dig = digest.digest((secureRandom.nextLong() + "").getBytes());
            return binaryToHex(dig);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static String generateBearerToken(String access_token) {
        return "Bearer " + access_token;
    }

    public static String parseBearerToken(String authHeader) {
        return authHeader.split(" ")[1];
    }

    /**
     * 암호화
     * @param message
     * @return
     * @throws Exception
     */
    public static String encrypt(String message,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        return OAuth2Util.binaryToHex(encrypted);
    }

    /**
     * 복호화
     * @param encrypted
     * @return
     * @throws Exception
     */
    public static String decrypt(String encrypted,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(OAuth2Util.hexToBinary(encrypted));
        String originalString = new String(original);
        return originalString;
    }
}
