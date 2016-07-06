package com.msp.messenger.controller;

import com.msp.messenger.bean.ChatRoomInfoBean;
import com.msp.messenger.bean.MakeRoomBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.service.push.PushHttpCallService;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.JsonObjectConverter;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 21..
 */
@Controller
public class ChatRoomController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    RedisUserService redisUserService;

    @Autowired(required = true)
    RdbUserService rdbUserService;

    @Autowired(required = true)
    PushHttpCallService pushHttpCallService;

    @RequestMapping(value = "/makeChatRoom.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String makeChatRoom(HttpServletRequest request, HttpServletResponse response, @ModelAttribute MakeRoomBean makeRoomBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        if(makeRoomBean.getUSERID().equals("") || makeRoomBean.getAPPID().equals("") || makeRoomBean.getINVITE_USERIDS().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, makeRoomBean.getAPPID());
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                TreeSet<Object> inviteUserIDTreeSet = new TreeSet<Object>();
                StringTokenizer st = new StringTokenizer(makeRoomBean.getINVITE_USERIDS(),",");
                while (st.hasMoreTokens()){
                    inviteUserIDTreeSet.add(st.nextToken().trim());
                }
                inviteUserIDTreeSet.add(makeRoomBean.getUSERID().trim());
                Set<String> pushSendUseridSet = new HashSet<String>();
                List<Object> userIDs = new ArrayList<Object>();
                List<String> userIDs_BrokerIDs = new ArrayList<String>();
                List<Object> hpNums = new ArrayList<Object>();
                String roomOwnerUserid = makeRoomBean.getUSERID();
                String roomOwnerNicname = makeRoomBean.getUSERID();
                if(inviteUserIDTreeSet.size()>1){
                    // 초대한 유저아이디가 실질적으로 메신저서비스 가입이되어 있는지 확인.
                    List<UserInfoBean> userInfoBeans = redisUserService.multiGetUserInfoFromUserIdset(makeRoomBean.getAPPID(),inviteUserIDTreeSet);
                    // 초대한 유저아이디갯수와 실직적으로 메신저서비스 가입되어 있는 유저수가 다를 경우 메신저 서비스에 가입되어 있는 유저만 대화방에 등록함.
                    inviteUserIDTreeSet = new TreeSet<Object>();
                    for(UserInfoBean userInfoBean : userInfoBeans){
                        String db_userID = userInfoBean.getUSERID();
                        String db_hpNum = userInfoBean.getHP_NUM();
                        inviteUserIDTreeSet.add(db_userID);
                        userIDs.add(db_userID);
                        userIDs_BrokerIDs.add(db_userID+"|"+userInfoBean.getBROKER_ID());
                        hpNums.add(db_hpNum);
                        if(!userInfoBean.getPUSH_TOKEN().equals("")){
                            // 푸시토큰을 등록한 사용자에 대화방 초대메세지를 보내기 위해
                            pushSendUseridSet.add(db_userID);
                        }
                        if(roomOwnerUserid.equals(db_userID)){
                            roomOwnerNicname = userInfoBean.getNICKNAME();
                        }
                    }
                }
                // 2인 이유는 본인 포함이므로...
                if(inviteUserIDTreeSet.size()<2){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_1001);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_1001_MSG+"(서비스에 가입된 초대유저가 없습니다.)");
                    return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
                }
                String chatRoomID = makeChatRoomID(inviteUserIDTreeSet);
                makeRoomBean.setROOMID(chatRoomID);
                //이미 만들어진 채팅방을 또 만드는지 검증
                String checkRoomID = redisUserService.isExistChatRoomTopic(makeRoomBean,inviteUserIDTreeSet.size());
                if(checkRoomID==null || !checkRoomID.equals(chatRoomID)) {
                    if(checkRoomID==null){
                        checkRoomID = chatRoomID;
                    }
                    makeRoomBean.setROOMID(checkRoomID);
                    // 챗팅방 만들기
                    redisUserService.makeChatRoom(makeRoomBean,userIDs,userIDs_BrokerIDs,hpNums,inviteUserIDTreeSet);
                    // 푸시를 이용하여 해당 사용자에 초대 메세지를 보낸다.
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    sb.append(roomOwnerNicname);
                    sb.append("]님이 대화방에 초대하였습니다.");
                    pushHttpCallService.pushSend(pushSendUseridSet,sb.toString(),makeRoomBean.getAPPID());
                }else{
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2003);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2003_MSG);
                }
                resultBodyMap.put("chatRoomInfo",makeRoomBean);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
        }

        return responseJson(resultHeadMap,resultBodyMap, makeRoomBean.getAPPID());
    }

    @RequestMapping(value="/chatRoomList.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomList(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_USERID = request.getParameter("USERID");

        if(req_APPID==null || req_APPID.equals("") || req_USERID==null || req_USERID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_APPID);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                List<ChatRoomInfoBean> chatRoomInfoJsonList = redisUserService.getChatRoomList(req_APPID, req_USERID);
                resultBodyMap.put("roomInfoList",chatRoomInfoJsonList);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, req_APPID);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    @RequestMapping(value="/chatRoomDetailInfo.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomDetailInfo(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_USERID = request.getParameter("USERID");
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_USERID==null || req_USERID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_APPID);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                ChatRoomInfoBean chatRoomInfoBean = redisUserService.getChatRoomDetailInfo(req_APPID, req_ROOMID);
                resultBodyMap.put("roomDetailInfo",chatRoomInfoBean);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, req_APPID);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    @RequestMapping(value="/chatRoomForceRemove.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomForceRemove(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_USERID = request.getParameter("USERID");
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_USERID==null || req_USERID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_APPID);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                redisUserService.forceRemoveChatRoom(req_APPID, req_ROOMID);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, req_APPID);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    @RequestMapping(value="/chatRoomGetOut.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomGetOut(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_USERID = request.getParameter("USERID");
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_USERID==null || req_USERID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, req_APPID);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                redisUserService.getOutChatRoom(req_APPID, req_USERID, req_ROOMID);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, req_APPID);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    // TODO:챗팅방 입장시 상세정보 API 구현

    private String responseJson(Map<String,String> resultHeadMap,Map<String,Object> resultBodyMap,String reqAPPID){
        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[UserController] :" + responseJson);
        return responseJson;
    }

    private String makeChatRoomID(TreeSet<Object> inviteUserIDTreeSet){
        StringBuffer sb = new StringBuffer();
        for(Object userIDObj : inviteUserIDTreeSet){
            sb.append(userIDObj.toString());
        }
        int sumByte = 0;
        char[] chatRoomChars = Hex.encodeHex(sb.toString().getBytes());
        String charRoomString = new String(chatRoomChars);


        byte[] cuidByteArr = charRoomString.getBytes();
        for (int i = 0; i < cuidByteArr.length; i++) {
            int aaa = cuidByteArr[i] << i;
            sumByte += aaa;
        }
        return sumByte+"";
    }
}
