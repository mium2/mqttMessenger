package com.msp.chat.server.commons.utill;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 8. 4..
 */
public class FileAuthUtils {

    public static String makeAccessToken(String userid, String filename, String ext) throws Exception{
        String accessToken = "";
        try {
            String next = getHmacSha256(filename + "&" + userid);
            next = next.substring(0, 16);
            String prev = encrypt(userid + "&" + filename + "&" + ext, next);
            accessToken = prev + "_" + next;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        return accessToken;
    }

    /**
     * 요청한 토큰의 validate 체크 API
     * @param access_token
     * @return
     */
    public static Map<String,String> validateAccessToken(String access_token){
        Map<String,String> resultMap = null;
        try {
            String[] temp = access_token.split("_");
            //클라이언트 인증 해시값
            String clientHashKey = temp[1];
            String base = decrypt(temp[0], clientHashKey);
            temp = base.split("&");
            String userid = temp[0];
            String filename = temp[1];
            String ext = temp[2];

            resultMap = new HashMap<String, String>();
            resultMap.put("userid",userid);
            resultMap.put("filename",filename);
            resultMap.put("ext",ext);

        }catch (Exception e){
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 암호화
     * @param message
     * @return
     * @throws Exception
     */
    private static String encrypt(String message,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        // Instantiate the cipher
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(message.getBytes());
        return binaryToHex(encrypted);
    }

    /**
     * 복호화
     * @param encrypted
     * @return
     * @throws Exception
     */
    private static String decrypt(String encrypted,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(hexToBinary(encrypted));
        String originalString = new String(original);
        return originalString;
    }

    private static String getHmacSha256(String str) {
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


    // byte[] to hex
    private static String binaryToHex(byte[] ba) {
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

    private static byte[] hexToBinary(String hex) {
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
}
