package com.msp.messenger.controller;

import com.msp.messenger.auth.MemoryTokenManager;
import com.msp.messenger.auth.vo.ReqAuthBaseVO;
import com.msp.messenger.bean.AppLicenseBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.license.LicenseValidator;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.JsonObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 6. 20..
 */
@Controller
public class AuthController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired(required = true)
    RedisUserService redisUserService;

    @Autowired(required = true)
    RdbUserService rdbUserService;

    @Autowired (required = true)
    MemoryTokenManager memoryTokenManager;

    @Value("${token.expire.minute:30}")
    private String TOKEN_EXPIRE_MINUTE;

    @RequestMapping(value = "/refreshToken.ctl", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public @ResponseBody
    String refreshToken(ReqAuthBaseVO rVO, HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,String> resultBodyMap = new HashMap<String, String>();
        if(rVO.getAPPID().equals("") || rVO.getDEVICEID().equals("") || rVO.getUSERID().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
        }

        try {
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap, request.getParameter("APPID")); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            // refresh token 생성
            AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(rVO.getAPPID());
            long currentTimeStamp = System.currentTimeMillis();
            long expireMilliSecond = currentTimeStamp + (Integer.parseInt(TOKEN_EXPIRE_MINUTE)*60*1000);
            String accessToken = memoryTokenManager.makeAccessToken(rVO.getUSERID(), rVO.getDEVICEID(), expireMilliSecond, appLicenseBean.getSECRET_KEY());
            resultBodyMap.put("token",accessToken);
            resultBodyMap.put("expire",""+expireMilliSecond);
        }catch(Exception e){
            e.printStackTrace();
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_3000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
        }
        return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
    }

    @RequestMapping(value = "/mobile/auth.ctl", method = RequestMethod.POST, produces = "application/json; charset=utf8")
    public @ResponseBody
    String authorizeUseMasterApp(ReqAuthBaseVO rVO, HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);
        Map<String,String> resultBodyMap = new HashMap<String, String>();
        try {
            if(rVO.getAPPID().equals("") || rVO.getDEVICEID().equals("") || rVO.getUSERID().equals("") || rVO.getMPSN().equals("")){
                resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
                return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
            }
            // 1. 앱아이디 라이센스 검증
            AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(rVO.getAPPID());
            if(appLicenseBean == null){
                resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_4001);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_4001_MSG);
                return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
            }
            // 2. MPSN 검증
            if(appLicenseBean==null || !appLicenseBean.getMPSN_KEY().equals(rVO.getMPSN())){
                resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_4002);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_4002_MSG);
                return responseJson(resultHeadMap,resultBodyMap, rVO.getAPPID());
            }

            // 3. client_id 존재여부 확인
            UserInfoBean userInfoBean = redisUserService.getUserInfo(rVO.getAPPID(),rVO.getUSERID());
            if(userInfoBean==null){
                // 서비스가 등록되지 않았으므로 임시토큰을 발급하고 해당 토큰은 서비스 가입시에만 유효하도록 처리
                // 서비스 가입이 되어 있지 않은 유저아이디 이므로 에러 처리
                //3-1. 임시 서비스 가입 token 생성
                long currentTimeStamp = System.currentTimeMillis();
                long expireMilliSecond = currentTimeStamp + (Integer.parseInt(TOKEN_EXPIRE_MINUTE)*60*1000);
                String accessToken = memoryTokenManager.makeAccessToken(Constants.TEMP_AUTH_USERID, rVO.getDEVICEID(), expireMilliSecond, appLicenseBean.getSECRET_KEY());
                resultBodyMap.put("token",accessToken);
                resultBodyMap.put("expire",""+expireMilliSecond);
                return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
            }

            // 4. 서비스 가입을 한 동일 디바이스 인지 체크
            if(!userInfoBean.getDEVICEID().equals(rVO.getDEVICEID())){
                resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_3001);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_3001_MSG);
                return responseJson(resultHeadMap,resultBodyMap, rVO.getAPPID());
            }

            //5. token 생성
            long currentTimeStamp = System.currentTimeMillis();
            long expireMilliSecond = currentTimeStamp + (Integer.parseInt(TOKEN_EXPIRE_MINUTE)*60*1000);
            String accessToken = memoryTokenManager.makeAccessToken(rVO.getUSERID(), rVO.getDEVICEID(), expireMilliSecond, appLicenseBean.getSECRET_KEY());

            resultBodyMap.put("token",accessToken);
            resultBodyMap.put("expire",""+expireMilliSecond);
        }catch(Exception e){
            e.printStackTrace();
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_3000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
        }
        return responseJson(resultHeadMap, resultBodyMap, rVO.getAPPID());
    }

    private String responseJson(Map<String,String> resultHeadMap,Map<String,String> resultBodyMap,String reqAPPID){
        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[AuthController] :" + responseJson);
        return responseJson;
    }

}
