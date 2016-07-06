package com.msp.messenger.auth;

import com.msp.messenger.bean.AppLicenseBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.license.LicenseValidator;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.OAuth2Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 6. 20..
 */
@Service
public class MemoryTokenManager {

    @Autowired(required = true)
    RedisUserService redisUserService;

    Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public String makeAccessToken(String userid, String deviceid, long expireTimeMillis, String license_secretKey) throws Exception{
        String accessToken = "";
            try {
                String prev = encrypt(expireTimeMillis + "&" + userid + "&" + deviceid, license_secretKey);
                String next = OAuth2Util.getHmacSha256(expireTimeMillis + "&" + userid + "&" + deviceid);
                next = next.substring(0, 16);
                accessToken = prev + "_" + next;
            } catch (Exception e) {
                throw new UnauthorizedException(e.getMessage());
            }
        return accessToken;
    }


    /**
     * 요청한 토큰의 validate 체크 API
     * @param access_token
     * @return
     */
    public Map<String,String> validateAccessToken(String access_token, String APPID, String reqUSERID){
        AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(APPID);
        Map<String,String> resultMap = new HashMap<String, String>();
        resultMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultMap.put(Constants.RESULT_MESSAGE_KEY, "OK");

        try {
            String[] temp = access_token.split("_");

            //클라이언트 인증 해시값
            String clientHash = temp[1];

            String base = decrypt(temp[0], appLicenseBean.getSECRET_KEY());
            temp = base.split("&");
            String expireTimeMillis = temp[0];
            String userid = temp[1];
            String deviceid = temp[2];

            if(!reqUSERID.equals(userid)){
                // 인증 실패.
                resultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3006);
                resultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3006_MSG+"(사용자아이디 위변조)");
                return resultMap;
            }

            // userid & license_secretKey 해쉬키값 비교
            String compareHash = OAuth2Util.getHmacSha256(expireTimeMillis + "&" + userid + "&" + deviceid).substring(0, 16);
            if (!compareHash.equals(clientHash)) {
                // 토큰이 위변조 되었으므로 인증 에러 처리.
                resultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3004);
                resultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3004_MSG);
                return resultMap;
            }

            // 해당 유저아이디 서비스 등록되어 있는 사용자 검증.
            UserInfoBean userInfoBean = redisUserService.getUserInfo(APPID,reqUSERID);

            if (userInfoBean != null && deviceid.equals(userInfoBean.getDEVICEID())) {
                // 토큰 유효시간 검증
                long long_expireTimeMillis = Long.parseLong(expireTimeMillis);
                if (long_expireTimeMillis < System.currentTimeMillis()) {
                    // 유효시간 만료 에러 처리
                    resultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3005);
                    resultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3005_MSG);
                }
            } else {
                // 인증 실패.
                resultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3006);
                resultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3006_MSG+"(DEVICE 인증오류)");
            }
        }catch (Exception e){
            e.printStackTrace();
            resultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3007);
            resultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3007_MSG+ "("+e.getMessage()+")");
        }
        return resultMap;
    }

    /**
     * 암호화
     * @param message
     * @return
     * @throws Exception
     */
    public String encrypt(String message,String secretKey) throws Exception {
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
    public String decrypt(String encrypted,String secretKey) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] original = cipher.doFinal(OAuth2Util.hexToBinary(encrypted));
        String originalString = new String(original);
        return originalString;
    }


}
