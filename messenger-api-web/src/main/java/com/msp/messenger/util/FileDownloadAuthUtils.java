package com.msp.messenger.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 8. 5..
 */
public class FileDownloadAuthUtils {
    private final static FileDownloadAuthUtils instance = new FileDownloadAuthUtils();
    private Set<String> CHKIMGSET = new HashSet<String>();

    private FileDownloadAuthUtils(){
        CHKIMGSET.add("jpg");
        CHKIMGSET.add("jpeg");
        CHKIMGSET.add("gif");
        CHKIMGSET.add("bmp");
        CHKIMGSET.add("png");
        CHKIMGSET.add("tif");
    }

    public static FileDownloadAuthUtils getInstance(){
        return instance;
    }

    public String makeDownloadToken(String userid, String filename, String ext, String chatroomid) throws Exception{
        String accessToken = "";
        try {
            String next = getHmacSha256(filename + "&" + ext + "&" + userid);
            next = next.substring(0, 16);
            String prev = encrypt(filename + "&" + ext + "&" + userid, chatroomid.substring(0,16));
            accessToken = prev + "_" + chatroomid + "_" + next;
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
    public Map<String,String> validateDownloadToken(String access_token){
        Map<String,String> resultMap = new HashMap<String, String>();
        resultMap.put("resultcode","200");
        resultMap.put("resultmsg","SUCCESS");
        try {
            String[] temp = access_token.split("_");
            //클라이언트 인증 해시값
            String clientHashKey = temp[2];
            String chatroomID = temp[1];
            String base = decrypt(temp[0], chatroomID.substring(0,16));
            temp = base.split("&");
            String filename = temp[0];
            String ext = temp[1];
            String userid = temp[2];

            String chkString = getHmacSha256(filename + "&" + ext + "&" + userid).substring(0,16);

            if(!clientHashKey.equals(chkString)){
                resultMap.put("resultcode","403");
                resultMap.put("resultmsg","파일엑세스토큰 인증에러");
                return resultMap;
            }
            resultMap.put("chatroomid",chatroomID);
            resultMap.put("publisher",userid);
            resultMap.put("filename",filename);
            resultMap.put("ext",ext);

        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("resultcode","500");
            resultMap.put("resultmsg",e.getMessage());
        }
        return resultMap;
    }

    public boolean chkImageFile(String extention){
        extention = extention.toLowerCase();
        return CHKIMGSET.contains(extention);
    }

    /**
     * 암호화
     * @param message
     * @return
     * @throws Exception
     */
    private String encrypt(String message,String secretKey) throws Exception {
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
    private String decrypt(String encrypted,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(hexToBinary(encrypted));
        String originalString = new String(original);
        return originalString;
    }

    private String getHmacSha256(String str) {
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
    private String binaryToHex(byte[] ba) {
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

    private byte[] hexToBinary(String hex) {
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
