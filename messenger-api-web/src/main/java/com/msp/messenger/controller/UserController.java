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
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 13..
 */
@Controller
public class UserController {
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


    /**
     * 유저정보 수정 API
     * @param request
     * @param response
     * @param req_userInfoBean
     * @return
     */
    @RequestMapping(value = "/editUserInfo.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String editUserInfo(HttpServletRequest request, HttpServletResponse response, @ModelAttribute UserInfoBean req_userInfoBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,String> resultBodyMap = new HashMap<String, String>();

        if(req_userInfoBean.getAPPID().equals("") || req_userInfoBean.getDEVICEID().equals("")
            || req_userInfoBean.getHP_NUM().equals("") || req_userInfoBean.getNICKNAME().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
        }
        try {
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID()); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            req_userInfoBean.setUSERID(userId);
            String hpNum = req_userInfoBean.getHP_NUM().replaceAll("-","").replaceAll(" ", "");
            req_userInfoBean.setHP_NUM(hpNum);

            // PASSWORD SHA256 Encrypt
            String encryptPass = Sha256Util.getEncrypt(req_userInfoBean.getPASSWORD());
            req_userInfoBean.setPASSWORD(encryptPass);

            // 이미 messenger 서비스에 등록되어 있는 사용자인지 확인 한다.
            UserInfoBean orgUserInfoBean = redisUserService.getUserInfo(req_userInfoBean.getAPPID(), userId);
            if(orgUserInfoBean==null){
                // 인증실패 에러 처리
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3008);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3008_MSG);
                return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
            }else{
                String orgHP_NUM = orgUserInfoBean.getHP_NUM();
                orgUserInfoBean.setPASSWORD(req_userInfoBean.getPASSWORD());
                orgUserInfoBean.setNICKNAME(req_userInfoBean.getNICKNAME());
                orgUserInfoBean.setHP_NUM(req_userInfoBean.getHP_NUM());
                // 이전 데이타에서 변경된 내용이 있으므로 RDB와 Redis 유저정보를 업데이트 한다.
                try {
                    int applyRow = rdbUserService.updateUser(orgUserInfoBean);
                    if (applyRow > 0) {
                        redisUserService.editUser(orgUserInfoBean,orgHP_NUM);
                    }
                } catch (Exception e) {
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
                    return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                }
            }

            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }catch(Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }
    }

    // messenger 서비스 가입.
    @Deprecated
    @RequestMapping(value = "/checkUserAndRegist.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String subscribeChatService(HttpServletRequest request, HttpServletResponse response, @ModelAttribute UserInfoBean req_userInfoBean){
        Map<String,String> resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
        if(resultHeadMap==null) {
            resultHeadMap = new HashMap<String, String>();
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.RESULT_CODE_OK);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.RESULT_MESSAGE_OK);
        }
        Map<String,String> resultBodyMap = new HashMap<String, String>();
        String returnAuthKey = request.getParameter("AUTHKEY");

        boolean isPushServiceApiCall = false; // 푸시서비스 가입 호출 여부

        if(req_userInfoBean.getAPPID().equals("") || req_userInfoBean.getDEVICEID().equals("")
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
                // 최초가입이므로 토큰이 임시토큰인지 확인 한다.(임시토큰 여부 resultHeadMap.get(Constants.RESULT_CODE_KEY) 값이 205 이어야 한다.)
                // AuthKey 검사결과 처리
                if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK) && !resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_HOLD_OK)) {  //임시발급 인증키가 아니면 인증에러리턴.
                    // 인증에러
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3000);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3000_MSG);
                    return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
                }
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
                //임시토큰인 아닌 지속적으로 사용가능 한 token 생성
                long currentTimeStamp = System.currentTimeMillis();
                long expireMilliSecond = currentTimeStamp + (Integer.parseInt(TOKEN_EXPIRE_MINUTE)*60*1000);
                returnAuthKey = memoryTokenManager.makeAccessToken(req_userInfoBean.getUSERID(), req_userInfoBean.getDEVICEID(), expireMilliSecond, appLicenseBean.getSECRET_KEY());
            } else {
                // AuthKey 검사결과 처리. 반드시 정상적인 200 코드여야 한다.  앱을 지웠다가 다시 설치하는 경우는 임시토큰이 발급된다.
                if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {
                    // 인증에러
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3000);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3000_MSG+"[temporary authkey]");
                    return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
                }
                // TODO : 패스워드 검증
                if(!encryptPass.equals(orgUserInfoBean.getPASSWORD())){
                    // 인증에러
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3009);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3009_MSG);
                    return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
                }

                // 최최 할당 브로커서버아이디는 계속 똑같이 유지시키기위해 아래와 같이 셋팅
                req_userInfoBean.setORG_BROKER_ID(orgUserInfoBean.getORG_BROKER_ID());
                // 이미 등록되어 있으나 핸드폰 번호가 저장되어 있는 것과 다르게 들어왔을때 처리 방안.
                // STEP1. 핸드폰번호가 다르다는 에러 메세지 전송하여 핸드폰 번호 인증을 다시 받도록 한다.
                if(!orgUserInfoBean.getHP_NUM().equals(req_userInfoBean.getHP_NUM())){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2002);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2002_MSG);
                    return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                }
                // STEP1. 최초 할당된 브로커서버아이디와 현재 할당된 브로커서버아이디가 같은지 확인하여 같을경우와 다를 경우를 확인한다.
                // STEP2. 최초 할당된 브로커서버가 서비스ON(정상화) 되었다면 원래 브로커서버아이디로 다시 셋팅한다.
                boolean isChangeSetBrokerServerID = false;
                if(!orgUserInfoBean.getBROKER_ID().equals(orgUserInfoBean.getORG_BROKER_ID())){
                    boolean statusOrgBrokerServer = RedisAllocateUserService.getInstance().statusBrokerService(orgUserInfoBean.getORG_BROKER_ID());
                    if(statusOrgBrokerServer){ //최초 할당된 브로커 서버가 ON으로 정상화 되었다면 다시 해당 최초 브로커 서버로 변경한다.
                        req_userInfoBean.setBROKER_ID(orgUserInfoBean.getORG_BROKER_ID());
                        RedisAllocateUserService.getInstance().changeBrokerUserCnt(orgUserInfoBean.getORG_BROKER_ID(), orgUserInfoBean.getBROKER_ID());
                        isChangeSetBrokerServerID = true;
                    }
                }

                if(!isChangeSetBrokerServerID) { //최초 브로커서버셋팅 로직을 타지 않았을 경우.
                    // STEP1. 이전 할당된 브로커의 서비스 상태를 확인 한다.
                    // STEP2. 서비스 상태가 OFF이면 기존 할당된 브로커서버에서 활성화된 다른 브로커 서버를 할당한다.
                    // STEP3. 서비스 상태가 ON이면 이전 할당된 브로커아이디를 그대로 셋팅한다.
                    boolean statusBrokerServer = RedisAllocateUserService.getInstance().statusBrokerService(orgUserInfoBean.getBROKER_ID());
                    if (statusBrokerServer) {
                        //기존 할당했던 서버가 서비스 상태 ON일때 기존 브로커 서버 그대로 할당.
                        req_userInfoBean.setBROKER_ID(orgUserInfoBean.getBROKER_ID());
                    } else {
                        //STEP1. 기존 할당했던 서버가 서비스 상태 아닐때 다른 브로커 서버를 할당한다.
                        // 연결할 브로커를 재 할당해 준다.
                        BrokerConInfoBean brokerConInfoBean = RedisAllocateUserService.getInstance().allocateBrokerConInfo();
                        if (brokerConInfoBean == null || brokerConInfoBean.getIP().equals("")) {
                            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, "REDIS에 Broker USER 할당할 서버가 없거나 RDB에 Broker서버 정보가 달라 에러가 발생했습니다.");
                            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
                        }
                        // 서비스중인 새로운 서버로 재 할당한다.
                        req_userInfoBean.setBROKER_ID(brokerConInfoBean.getSERVERID());
                        //STEP2. 할당된 Broker 유저카운트를 하나 증가시키고 서비스를 중지된 Brodker은 유저카운트를 감소시킨다.
                        RedisAllocateUserService.getInstance().changeBrokerUserCnt(brokerConInfoBean.getSERVERID(), orgUserInfoBean.getBROKER_ID());
                        isChangeSetBrokerServerID = true;
                    }
                }

                boolean isChangeToken = false;
                if(!orgUserInfoBean.getPUSH_TOKEN().equals(req_userInfoBean.getPUSH_TOKEN())){
                    isChangeToken = true;
                }

                // DEVICEID,HP,NICKNAME 변경되었는지 확인 하여 RDB 업데이트 유무 결정
                if (!isChangeToken && orgUserInfoBean.getNICKNAME().equals(req_userInfoBean.getNICKNAME())
                    && orgUserInfoBean.getHP_NUM().equals(req_userInfoBean.getHP_NUM())
                    && orgUserInfoBean.getDEVICEID().equals(req_userInfoBean.getDEVICEID())
                    && orgUserInfoBean.getBROKER_ID().equals(req_userInfoBean.getBROKER_ID())
                    && orgUserInfoBean.getPASSWORD().equals(req_userInfoBean.getPASSWORD())) {
                    // 변경된 내용이 없으므로 아무것도 하지 않아도 된다.

                } else {
                    // 푸시 토큰 값이 있고 푸시서비스에 토큰의 변경되었을 경우 수정 등록한다.
                    if(isPushServiceApiCall && isChangeToken){
                        // 푸시서버 API를 호출한다.
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
                    // 이전 데이타에서 변경된 내용이 있으므로 RDB와 Redis 유저정보를 업데이트 한다.
                    try {
                        int applyRow = rdbUserService.updateUser(req_userInfoBean);
                        if (applyRow > 0) {
                            redisUserService.editUser(req_userInfoBean,orgUserInfoBean.getHP_NUM());
                        }
                    } catch (Exception e) {
                        resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
                        resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
                        return responseJson(resultHeadMap,resultBodyMap, req_userInfoBean.getAPPID());
                    }

                    if(isChangeSetBrokerServerID){
                        redisUserService.chgSubscribeServerID(req_userInfoBean.getAPPID(),req_userInfoBean.getUSERID(),orgUserInfoBean.getBROKER_ID(),req_userInfoBean.getBROKER_ID());
                    }
                }
            }
            // 최초가입 또는 기존에 할당된 브로커아이디가 변경되었다면 USER_BROKERID(해당유저가 할당되어 있는 브로커서버아이디정보) 레디스 키테이블에 변경저장
            if(orgUserInfoBean==null || !orgUserInfoBean.getBROKER_ID().equals(req_userInfoBean.getBROKER_ID())){
                redisUserService.putLoginID(req_userInfoBean.getUSERID(), req_userInfoBean.getBROKER_ID());
            }
            // 할당할 브로커서버 정보를 보내야 한다.
            BrokerConInfoBean brokerConInfoBean = RedisAllocateUserService.getInstance().getClientConnectUpnsInfo(req_userInfoBean.getBROKER_ID());
            if(brokerConInfoBean==null){
                throw new Exception("접속 할 브로커아이디가 ("+req_userInfoBean.getBROKER_ID()+") DB에 등록되지 않은 브로커아이디 입니다.");
            }
            resultBodyMap.put("brokerip",brokerConInfoBean.getIP());
            resultBodyMap.put("port",brokerConInfoBean.getPORT());
            resultBodyMap.put("authkey",returnAuthKey);

            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }catch(Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_userInfoBean.getAPPID());
        }
    }


    private String responseJson(Map<String,String> resultHeadMap,Map<String,String> resultBodyMap,String reqAPPID){

        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[UserController] :" + responseJson);
        return responseJson;
    }



}
