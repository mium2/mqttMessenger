package com.msp.messenger.controller;

import com.msp.messenger.auth.MemoryTokenManager;
import com.msp.messenger.bean.AppLicenseBean;
import com.msp.messenger.bean.BrokerConInfoBean;
import com.msp.messenger.bean.LoginBean;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Y.B.H(mium2) on 16. 9. 13..
 */
@Controller
public class LoginController {
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

    @RequestMapping(value = "/login.ctl", method = RequestMethod.POST,produces = "application/json; charset=utf8")
    public @ResponseBody String login(HttpServletRequest request, HttpServletResponse response, @ModelAttribute LoginBean loginBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);
        Map<String,String> resultBodyMap = new HashMap<String, String>();

        boolean isPushServiceApiCall = false; // 푸시서비스 가입 호출 여부

        if(loginBean.getUSERID().equals("") || loginBean.getAPPID().equals("") || loginBean.getPASSWORD().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap);
        }
        if(!loginBean.getPUSH_TOKEN().trim().equals("") && !loginBean.getPUSH_SERVER().trim().equals("")) {
            isPushServiceApiCall = true;
        }

        // PASSWORD SHA256 Encrypt
        String encryptPass = Sha256Util.getEncrypt(loginBean.getPASSWORD());
        loginBean.setPASSWORD(encryptPass);

        String isRegistered = "N";
        // 서비스 가입되어 있는 존재하는 아이디 검증
        UserInfoBean orgUserInfoBean = redisUserService.getUserInfo(loginBean.getAPPID(), loginBean.getUSERID());
        if(orgUserInfoBean!=null){
            isRegistered = "Y";
        }
        try {
            String returnAuthKey= null;
            String expireDate = "0";
            // 아이디가 존재하지 않을 경우
            if (isRegistered.equals("N")) {
                // 인증에러
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3009);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3009_MSG);
                return responseJson(resultHeadMap, resultBodyMap);
            } else {
                // 패스워드 검증
                if(!encryptPass.equals(orgUserInfoBean.getPASSWORD())){
                    // 인증에러
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3009);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3009_MSG);
                    return responseJson(resultHeadMap, resultBodyMap);
                }else{
                    //인증 성공 토큰 생성
                    AppLicenseBean appLicenseBean = LicenseValidator.getInstance().getAppLicenseBean(loginBean.getAPPID());
                    long currentTimeStamp = System.currentTimeMillis();
                    long expireMilliSecond = currentTimeStamp + (Integer.parseInt(TOKEN_EXPIRE_MINUTE)*60*1000);
                    expireDate = ""+expireMilliSecond;
                    returnAuthKey = memoryTokenManager.makeAccessToken(loginBean.getUSERID(), loginBean.getDEVICEID(), expireMilliSecond, appLicenseBean.getSECRET_KEY());
                }

                // 최최 할당 브로커서버아이디는 계속 똑같이 유지시키기위해 아래와 같이 셋팅
                String preAllocateBrokerID = orgUserInfoBean.getBROKER_ID();
                String newAllocateBrokerID = "";

                // STEP1. 최초 할당된 브로커서버아이디와 현재 할당된 브로커서버아이디가 같은지 확인하여 같을경우와 다를 경우를 확인한다.
                // STEP2. 최초 할당된 브로커서버가 서비스ON(정상화) 되었다면 원래 브로커서버아이디로 다시 셋팅한다.
                boolean isChangeSetBrokerServerID = false;
                if(!"".equals(orgUserInfoBean.getORG_BROKER_ID()) && !orgUserInfoBean.getBROKER_ID().equals(orgUserInfoBean.getORG_BROKER_ID())){
                    // 오리지날 할당 받은 브로커서버가 살아있는지 체크
                    boolean statusOrgBrokerServer = RedisAllocateUserService.getInstance().statusBrokerService(orgUserInfoBean.getORG_BROKER_ID());
                    if(statusOrgBrokerServer){ //최초 할당된 브로커 서버가 ON으로 정상화 되었다면 다시 해당 최초 브로커 서버로 변경한다.
                        newAllocateBrokerID = orgUserInfoBean.getORG_BROKER_ID();
                        RedisAllocateUserService.getInstance().changeBrokerUserCnt(orgUserInfoBean.getORG_BROKER_ID(), orgUserInfoBean.getBROKER_ID());
                        orgUserInfoBean.setBROKER_ID(orgUserInfoBean.getORG_BROKER_ID());
                        orgUserInfoBean.setORG_BROKER_ID("");
                        isChangeSetBrokerServerID = true;
                    }
                }

                if(!isChangeSetBrokerServerID) { //최초 브로커서버셋팅 로직을 타지 않았을 경우.
                    // STEP1. 이전 할당된 브로커의 서비스 상태를 확인 한다.
                    // STEP2. 서비스 상태가 OFF이면 기존 할당된 브로커서버에서 활성화된 다른 브로커 서버를 할당한다.
                    // STEP3. 서비스 상태가 ON이면 이전 할당된 브로커아이디를 그대로 셋팅한다.
                    boolean statusBrokerServer = RedisAllocateUserService.getInstance().statusBrokerService(orgUserInfoBean.getBROKER_ID());
                    // 할당할 브로커 서버가 죽어있을 경우 다른 브로커 서버를 할당한다.
                    if (!statusBrokerServer) {
                        //STEP1. 기존 할당했던 서버가 서비스 상태 OFF 상태이므로  다른 브로커 서버를 할당한다.
                        // 연결할 브로커를 재 할당해 준다.
                        BrokerConInfoBean brokerConInfoBean = RedisAllocateUserService.getInstance().allocateBrokerConInfo();
                        if (brokerConInfoBean == null || brokerConInfoBean.getIP().equals("")) {
                            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, "REDIS에 Broker USER 할당할 서버가 없거나 RDB에 Broker서버 정보가 달라 에러가 발생했습니다.");
                            return responseJson(resultHeadMap, resultBodyMap);
                        }
                        // 서비스중인 새로운 서버로 재 할당한다.
                        newAllocateBrokerID= brokerConInfoBean.getSERVERID();
                        //STEP2. 할당된 Broker 유저카운트를 하나 증가시키고 서비스를 중지된 Brodker은 유저카운트를 감소시킨다.
                        RedisAllocateUserService.getInstance().changeBrokerUserCnt(newAllocateBrokerID, preAllocateBrokerID);

                        // 새로 할당 받은 브로커아이디가 오리지널브로커일 경우와 아닐 경우로 구분하여 셋팅
                        if(newAllocateBrokerID.equals(orgUserInfoBean.getORG_BROKER_ID())){
                            // 오리지널 브로커로 할당 받았을 경우
                            orgUserInfoBean.setBROKER_ID(newAllocateBrokerID);
                            orgUserInfoBean.setORG_BROKER_ID("");
                        }else{
                            orgUserInfoBean.setBROKER_ID(newAllocateBrokerID);
                            orgUserInfoBean.setORG_BROKER_ID(preAllocateBrokerID);
                        }

                        isChangeSetBrokerServerID = true;
                    }
                }

                // 푸시 토큰 변경여부 체크
                boolean isChangeToken = false;
                if(!orgUserInfoBean.getPUSH_TOKEN().equals(loginBean.getPUSH_TOKEN())){
                    isChangeToken = true;
                }

                // 푸시 토큰 값이 있고 푸시서비스에 토큰의 변경되었을 경우 수정 등록한다.
                if(isPushServiceApiCall && isChangeToken){
                    // 푸시서버 API를 호출한다.
                    orgUserInfoBean.setPUSH_TOKEN(loginBean.getPUSH_TOKEN());
                    orgUserInfoBean.setPUSH_SERVER(loginBean.getPUSH_SERVER());
                    try {
                        String APNS_MODE= "REAL";
                        if(!loginBean.getAPNS_MODE().equals("")){
                            APNS_MODE = loginBean.getAPNS_MODE();
                        }
                        Map<String, Object> responseMap = pushHttpCallService.callRegPushServiceAndUser(orgUserInfoBean,APNS_MODE);
                        Map<String, String> responseHeader = (Map<String, String>) responseMap.get("HEADER");
                        if (!responseHeader.get("RESULT_CODE").equals("0000")) {
                            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, responseHeader.get("RESULT_BODY"));
                            return responseJson(resultHeadMap,resultBodyMap);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_5000);
                        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_5000_MSG+"["+e.getMessage()+"]");
                        return responseJson(resultHeadMap,resultBodyMap);
                    }
                }

                // 할당 브로커가 변경되었을 경우 업데이트 처리 한다.
                if(isChangeSetBrokerServerID){
                    try {
                        int applyRow = rdbUserService.updateUser(orgUserInfoBean);
                        if (applyRow > 0) {
                            redisUserService.editUser(orgUserInfoBean, orgUserInfoBean.getHP_NUM());
                        }
                    } catch (Exception e) {
                        resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
                        return responseJson(resultHeadMap,resultBodyMap);
                    }

                    // 채팅방아이디에 참여하는 유저아이디|브로커아이디 정보 갱신
                    redisUserService.chgSubscribeServerID(orgUserInfoBean.getAPPID(), orgUserInfoBean.getUSERID(), preAllocateBrokerID, newAllocateBrokerID);
                }
            }
            // REDIS에 로그인한 아이디와 할당된 브로코아이디를 저장한다.
            redisUserService.putLoginID(orgUserInfoBean.getUSERID(), orgUserInfoBean.getBROKER_ID());


            // 할당할 브로커서버 정보를 보내야 한다.
            BrokerConInfoBean brokerConInfoBean = RedisAllocateUserService.getInstance().getClientConnectUpnsInfo(orgUserInfoBean.getBROKER_ID());
            resultBodyMap.put("brokerip",brokerConInfoBean.getIP());
            resultBodyMap.put("port",brokerConInfoBean.getPORT());
            resultBodyMap.put("authkey",returnAuthKey);
            resultBodyMap.put("expire",expireDate);

            return responseJson(resultHeadMap, resultBodyMap);
        }catch(Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }
    }

    @RequestMapping(value = "/logout.ctl", method = RequestMethod.POST,produces = "application/json; charset=utf8")
    public @ResponseBody String logout(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,String> resultBodyMap = new HashMap<String, String>();
        try{
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            redisUserService.rmLoginID(userId);

        }catch(Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }
        return responseJson(resultHeadMap, resultBodyMap);
    }

    private String responseJson(Map<String,String> resultHeadMap, Map<String,String> resultBodyMap){

        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        return responseJson;
    }
}
