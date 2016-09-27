package com.msp.messenger.controller;

import com.msp.messenger.auth.MemoryTokenManager;
import com.msp.messenger.bean.AppLicenseBean;
import com.msp.messenger.bean.BrokerConInfoBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.license.LicenseValidator;
import com.msp.messenger.service.push.PushHttpCallService;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisAllocateUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.JsonObjectConverter;
import com.msp.messenger.util.security.Sha256Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Y.B.H(mium2) on 16. 9. 19..
 * 메신저 서비스 가입 및 탈퇴
 */
@Controller
public class RegMessengerController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    private Properties myProperties;

    @Autowired(required = true)
    RedisUserService redisUserService;

    @Autowired(required = true)
    RdbUserService rdbUserService;

    @Autowired(required = true)
    MemoryTokenManager memoryTokenManager;

    @Autowired(required = true)
    PushHttpCallService pushHttpCallService;

    @Value("${token.expire.minute:30}")
    private String TOKEN_EXPIRE_MINUTE;

    // 아이디 사용가능 여부 체크 API
    @RequestMapping(value = "/checkUserID.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String regMessengerService(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,String> resultBodyMap = new HashMap<String, String>();
        String req_appid = request.getParameter("APPID");
        String req_mpsn = request.getParameter("MPSN");
        String req_userid = request.getParameter("USERID");

        resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);

        if(req_appid==null || req_mpsn==null || req_userid==null){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap,req_appid);
        }

        // 인증검사
        AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(req_appid);

        if(appLicenseBean==null || !appLicenseBean.getMPSN_KEY().equals(req_mpsn)){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_3000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_3000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_appid);
        }
        // 이미 등록된 아이디 여부 체크 로직 구현
        String isUsed = "N";
        UserInfoBean orgUserInfoBean = redisUserService.getUserInfo(req_appid, req_userid);
        if(orgUserInfoBean!=null){
            isUsed = "Y";
        }
        resultBodyMap.put("isUsed",isUsed);
        return responseJson(resultHeadMap,resultBodyMap, req_appid);
    }

    // messenger 서비스 가입.
    @RequestMapping(value = "/regMessengerService.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String regMessengerService(HttpServletRequest request, HttpServletResponse response, @ModelAttribute UserInfoBean req_userInfoBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);
        Map<String,String> resultBodyMap = new HashMap<String, String>();

        boolean isPushServiceApiCall = false; // 푸시서비스 가입 호출 여부

        if(req_userInfoBean.getUSERID().equals("") || req_userInfoBean.getAPPID().equals("") || req_userInfoBean.getDEVICEID().equals("")
            || req_userInfoBean.getHP_NUM().equals("") || req_userInfoBean.getNICKNAME().equals("") || req_userInfoBean.getMPSN().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
        }
        if(!req_userInfoBean.getPUSH_TOKEN().trim().equals("") && !req_userInfoBean.getPUSH_SERVER().trim().equals("")) {
            isPushServiceApiCall = true;
        }

        String APNS_MODE= "REAL";
        if(request.getParameter("APNS_MODE")!=null){
            APNS_MODE = request.getParameter("APNS_MODE").trim();
        }
        // 인증검사
        AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(req_userInfoBean.getAPPID());

        if(appLicenseBean==null || !appLicenseBean.getMPSN_KEY().equals(req_userInfoBean.getMPSN())){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_3000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_3000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
        }

        String hpNum = req_userInfoBean.getHP_NUM().replaceAll("-","").replaceAll(" ","");
        req_userInfoBean.setHP_NUM(hpNum);

        // PASSWORD SHA256 Encrypt
        String encryptPass = Sha256Util.getEncrypt(req_userInfoBean.getPASSWORD());
        req_userInfoBean.setPASSWORD(encryptPass);

        String isRegistered = "N";
        // 이미 messenger 서비스에 등록되어 있는 사용자인지 확인 한다.
        UserInfoBean orgUserInfoBean = redisUserService.getUserInfo(req_userInfoBean.getAPPID(), req_userInfoBean.getUSERID());
        if(orgUserInfoBean!=null){
            isRegistered = "Y";
        }

        try {
            // 유저등록(RDB / Redis)
            if (!isRegistered.equals("Y")) {
                //넘어온 핸드폰 번호가 다른 아이디가 사용중일때 처리방법.
                //STEP1. 이미 사용중인 핸드폰 번호이므로 에러 처리를 하여 핸드폰번호 인증을 받도록 처리하도록 가이드.
                //STEP2. 인증을 받은 후 해당 핸드폰 번호를 사용 할 수 있도록 로직 구현해야함.
                //STEP3. 클라이언트는 라이센스시크릿키로 핸드폰번호와CUID를 암호화하고 해당 hash값을 넘기면 서버는 넘어온 암호화 값의 위변조를 검사하고 복호화 한 후 해당 값으로 기존값을 변경또는 삭제 후 저장한다.
                if(redisUserService.isExistHpNum(req_userInfoBean.getAPPID(),req_userInfoBean.getHP_NUM())){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2001);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2001_MSG);
                    return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                }
                // 연결할 브로커를 할당해 준다.
                BrokerConInfoBean brokerConInfoBean = RedisAllocateUserService.getInstance().allocateBrokerConInfo();
                if(brokerConInfoBean==null || brokerConInfoBean.getIP().equals("")){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, "REDIS에 Broker USER 할당할 서버가 없거나 RDB에 Broker서버 정보가 달라 에러가 발생했습니다.");
                    return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                }
                req_userInfoBean.setBROKER_ID(brokerConInfoBean.getSERVERID());
                req_userInfoBean.setORG_BROKER_ID(brokerConInfoBean.getSERVERID());
                logger.debug("##### APPID: "+req_userInfoBean.getAPPID()+"   BROKER_ID : "+req_userInfoBean.getBROKER_ID());
                // RDB 저장, Redis 유저정보 저장
                try {
                    // 푸시토큰과 푸시 서버종류 값이 넘어오면 푸시서비스를 가입 시킨다.
                    if(isPushServiceApiCall) {
                        // 푸시서버에 토큰 등록 API를 호출한다.
                        try {
                            Map<String, Object> responseMap = pushHttpCallService.callRegPushServiceAndUser(req_userInfoBean, APNS_MODE);
                            Map<String, String> responseHeader = (Map<String, String>) responseMap.get("HEADER");
                            if (!responseHeader.get("RESULT_CODE").equals("0000")) {
                                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, responseHeader.get("RESULT_BODY"));
                                return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_5000_MSG+"["+e.getMessage()+"]");
                            return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                        }
                    }

                    int applyRow = rdbUserService.regUser(req_userInfoBean);
                    if (applyRow > 0) {
                        redisUserService.putUser(req_userInfoBean);
                        // 할당된 브로커 서버에 할당유저수 증가 시킴.
                        RedisAllocateUserService.getInstance().plusUpnsUserCnt(req_userInfoBean.getBROKER_ID());
                    }
                } catch (Exception e) {
                    try {
                        // RDB에는 데이타가 있으나 REDIS에는 데이타가 없는 경우 에러가 날 수 있으므로 RDB유저 삭제 후 동기화 처리
                        rdbUserService.delUser(req_userInfoBean);
                        int applyRow = rdbUserService.regUser(req_userInfoBean);
                        if (applyRow > 0) {
                            redisUserService.putUser(req_userInfoBean);
                            // 할당된 브로커 서버에 할당유저수 증가 시킴.
                            RedisAllocateUserService.getInstance().plusUpnsUserCnt(req_userInfoBean.getBROKER_ID());
                        }
                    }catch (Exception e2){
                        e.printStackTrace();
                        resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_5000_MSG+"["+e2.getMessage()+"]");
                        return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                    }
                }
            } else {
                // 이미 서비스 가입이 되어 있는 아이디라는 에러메세지 보냄.
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_1003);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_1003_MSG);
                return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
            }

            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }catch(Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }
    }


    @RequestMapping(value="/unRegistUser.ctl", produces = "application/json; charset=utf8")
    public @ResponseBody
    String unRegistUser(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,String> resultBodyMap = new HashMap<String, String>();

        String req_APPID = request.getParameter("APPID");

        if(req_APPID==null || req_APPID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_APPID);
        }
        try {
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap, req_APPID); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            // 푸시서버에 토큰 등록 API를 호출한다.
            try {
                UserInfoBean userInfoBean = redisUserService.getUserInfo(req_APPID, userId);
                if(userInfoBean==null){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1002);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1002_MSG);
                    return responseJson(resultHeadMap,resultBodyMap, req_APPID);
                }
                if(!userInfoBean.getPUSH_TOKEN().trim().equals("") && !userInfoBean.getPUSH_SERVER().trim().equals("")) {
                    Map<String, Object> responseMap = pushHttpCallService.unRegistPush(userInfoBean.getAPPID(), userInfoBean.getDEVICEID(), userInfoBean.getPUSH_SERVER(), userInfoBean.getUSERID(), userInfoBean.getPUSH_TOKEN());
                    //TODO: 고민? 푸시서비스 가입을 정상 삭제 성공여부에 따라 메신저 유저를 삭제 할 필요는 없어 보임.
//                        Map<String, String> responseHeader = (Map<String, String>) responseMap.get("HEADER");
//                        if (!responseHeader.get("RESULT_CODE").equals("0000")) {
//                            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
//                            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, responseHeader.get("RESULT_BODY"));
//                            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
//                        }
                }

                int applyRow = rdbUserService.delUser(userInfoBean);
                if (applyRow > 0) {
                    redisUserService.removeUser(userInfoBean);
                    // 할당된 브로커 서버에 할당유저수 증가 시킴.
                    RedisAllocateUserService.getInstance().minusUpnsUserCnt(userInfoBean.getBROKER_ID());
                }

            }catch (Exception e){
                e.printStackTrace();
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_5000_MSG+"["+e.getMessage()+"]");
                return responseJson(resultHeadMap,resultBodyMap, req_APPID);
            }

        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    private String responseJson(Map<String,String> resultHeadMap,Map<String,String> resultBodyMap,String reqAPPID){

        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        if(resultBodyMap.size()>0) {
            returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        }
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[UserController] :" + responseJson);
        return responseJson;
    }
}
