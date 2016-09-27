package com.msp.messenger.controller;

import com.msp.messenger.bean.ChatRoomInfoBean;
import com.msp.messenger.bean.MakeRoomBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.service.push.PushHttpCallService;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.HexUtil;
import com.msp.messenger.util.JsonObjectConverter;
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

    /**
     * 설명 : 챗팅방 타입은 두가지로 나눌 수 있는데.
     * 첫번째 1:1방.  요건 : 해당방은 두명의 유저아이디로 항상 동일한 룸아이디를 만들어야 한다.
     * 두번째 그룹방.  요건 : 해당 룸아이디는 유저아이디와 상관없이 룸아이디를 고유하게 만들어 초대하고 나가도 룸아이디 변화가 없어야 한다.
     * ROOMTYPE : E ==> 1:1챗팅방, G ==> 그룹방
     * @param request
     * @param response
     * @param makeRoomBean
     * @return
     */
    @RequestMapping(value = "/makeChatRoom.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String makeChatRoom(HttpServletRequest request, HttpServletResponse response, @ModelAttribute MakeRoomBean makeRoomBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        if(makeRoomBean.getINVITE_USERIDS().equals("") || makeRoomBean.getROOMTYPE().equals("") || makeRoomBean.getROOMNAME().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, makeRoomBean.getAPPID());
        }
        try {
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID()); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            String createdRoomID = null;
            String roomOwnerUserid = "";
            String roomOwnerNicname = "";
            TreeSet<Object> reqInviteUserIDTreeSet = new TreeSet<Object>(); // 초대유저 TreeSet
            makeRoomBean.setUSERID(userId); // 방만들기 요청

            // 푸쉬발송
            Set<String> pushSendUseridSet = new HashSet<String>();
            List<Object> userIDs = new ArrayList<Object>();
            List<String> userIDs_BrokerIDs = new ArrayList<String>();
            List<Object> hpNums = new ArrayList<Object>();

            // 1:1방 만들기
            if(makeRoomBean.getROOMTYPE().equals("E")){
                reqInviteUserIDTreeSet.add(userId);
                reqInviteUserIDTreeSet.add(makeRoomBean.getINVITE_USERIDS());
                createdRoomID = makeOneToOneChatRoomID(reqInviteUserIDTreeSet);
            // 그룹방 만들기
            }else if(makeRoomBean.getROOMTYPE().equals("G")){
                // 그룹 챗팅방ID를 만들기 요청
                createdRoomID = makeGroupChatRoomID(makeRoomBean.getAPPID(),userId);
                StringTokenizer st = new StringTokenizer(makeRoomBean.getINVITE_USERIDS(),",");
                while(st.hasMoreTokens()){
                    reqInviteUserIDTreeSet.add(st.nextToken().trim());
                }
                reqInviteUserIDTreeSet.add(userId); //그룹방 만드는 자기 자신 추가.
            }else{
                // 에러 처리
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2004);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2004_MSG);
                return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
            }

            makeRoomBean.setROOMID(createdRoomID); // 만들어진 룸아이디를 set 한다.
            TreeSet<Object> inviteUserIDTreeSet = new TreeSet<Object>();
            List<UserInfoBean> userInfoBeans = redisUserService.multiGetUserInfoFromUserIdset(makeRoomBean.getAPPID(),reqInviteUserIDTreeSet);

            // 초대한 유저아이디갯수와 실직적으로 메신저서비스 가입되어 있는 유저수가 다를 경우 메신저 서비스에 가입되어 있는 유저만 대화방에 등록함.
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

            // 여기서 그룹방일 경우는 유저수 체크하지 않고.. 1:1 챗팅방일 경우는 유저수 체크하는 로직을 넣어야 한다.
            if("E".equals(makeRoomBean.getROOMTYPE())){
                // 이미 만들어져 있는 1:1방인지 검증
                ChatRoomInfoBean chatRoomInfoBean = redisUserService.isExistChatRoom(makeRoomBean);
                if(chatRoomInfoBean!=null){
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2003);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2003_MSG);
                    resultBodyMap.put("chatRoomInfo",makeRoomBean);
                    return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
                }
                if(userInfoBeans.size()<=1){
                    // 1:1방에서 초대유저가 실질적으로 서비스 가입이 되어 있지 않은 경우 에러처리
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2006);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2006_MSG);
                    return responseJson(resultHeadMap, resultBodyMap, makeRoomBean.getAPPID());
                }
            }

            // 챗팅방 만들기.
            redisUserService.makeChatRoom(makeRoomBean, userIDs, userIDs_BrokerIDs, hpNums, inviteUserIDTreeSet);

            // 푸시를 이용하여 해당 사용자에 초대 메세지를 보낸다.
            pushSendUseridSet.remove(userId); //방을 만든 아이디는 푸시발송에서 제외시킴.
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            sb.append(roomOwnerNicname);
            sb.append("]님이 대화방에 초대하였습니다.");
            pushHttpCallService.pushSend(pushSendUseridSet, sb.toString(), makeRoomBean.getAPPID(),makeRoomBean.getROOMID());
            resultBodyMap.put("chatRoomInfo",makeRoomBean);

        }catch (Exception e){
            e.printStackTrace();
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

            List<ChatRoomInfoBean> chatRoomInfoJsonList = redisUserService.getChatRoomList(req_APPID, userId);
            resultBodyMap.put("roomInfoList",chatRoomInfoJsonList);

        }catch (Exception e){
            e.printStackTrace();
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
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
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

            // 비즈니스 로직수 행
            ChatRoomInfoBean chatRoomInfoBean = redisUserService.getChatRoomDetailInfo(req_APPID, req_ROOMID);
            if(chatRoomInfoBean==null){
                resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_2007);
                resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_2007_MSG);
                return responseJson(resultHeadMap, resultBodyMap, req_APPID);
            }else {
                resultBodyMap.put("roomDetailInfo", chatRoomInfoBean);
            }

        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    /**
     * 대화방 삭제 : 그룹방일 경우 방장이 해당 그룹방을 삭제 할 수 있도록 처리.
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value="/chatRoomForceRemove.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomForceRemove(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
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
            ChatRoomInfoBean chatRoomInfoBean = redisUserService.getChatRoomDetailInfo(req_APPID,userId);
            if(chatRoomInfoBean==null){
                throw new Exception("존재하지 않는 대화방아이디 입니다.");
            }else {
                if(chatRoomInfoBean.getROOM_OWNER().equals(userId)) {
                    redisUserService.forceRemoveChatRoom(req_APPID, req_ROOMID);
                }else {
                    throw new Exception("방장만이 해당 방을 삭제 할 수 있습니다.");
                }
            }

        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    /**
     * 방장 변경 API
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value="/changeRoomOwner.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chageRoomOwner(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_ROOMID = request.getParameter("ROOMID");
        String req_NEW_ROOM_OWNER = request.getParameter("NEW_ROOM_OWNER");

        if(req_APPID==null || req_APPID.equals("") || req_ROOMID==null || req_ROOMID.equals("") || req_NEW_ROOM_OWNER==null || req_NEW_ROOM_OWNER.equals("")){
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
            if(req_ROOMID.startsWith("E")){
                throw new Exception("1:1방은 방장을 변경 할 수 없습니다.");
            }

            ChatRoomInfoBean chatRoomInfoBean = redisUserService.getChatRoomDetailInfo(req_APPID, userId);
            if(chatRoomInfoBean==null){
                throw new Exception("존재하지 않는 대화방아이디 입니다.");
            }else {
                if(chatRoomInfoBean.getROOM_OWNER().equals(userId)) {
                    // REDIS 방장 정보변경
                    redisUserService.changeRoomOwner(req_APPID, chatRoomInfoBean, req_NEW_ROOM_OWNER);
                }else {
                    throw new Exception("방장만이 새로운 방장을 위임 할 수 있습니다.");
                }
            }

        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }

    /**
     * 방나가기 : 마지막 한명까지 다 나가면 방을 소멸 시킨다.
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value="/chatRoomGetOut.ctl",produces="application/json; charset=utf8")
    public @ResponseBody
    String chatRoomGetOut(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_APPID = request.getParameter("APPID");
        String req_ROOMID = request.getParameter("ROOMID");

        if(req_APPID==null || req_APPID.equals("") || req_ROOMID==null || req_ROOMID.equals("")){
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

            redisUserService.getOutChatRoom(req_APPID, userId, req_ROOMID);

        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, req_APPID);
        }
        return responseJson(resultHeadMap,resultBodyMap, req_APPID);
    }


    private String responseJson(Map<String,String> resultHeadMap,Map<String,Object> resultBodyMap,String reqAPPID){
        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[UserController] :" + responseJson);
        return responseJson;
    }

    private String makeOneToOneChatRoomID(TreeSet reqInviteUserIDTreeSet) throws Exception{
        StringBuffer sb = new StringBuffer();
        for(Object userIDObj : reqInviteUserIDTreeSet){
            sb.append(userIDObj.toString());
        }
        String makeRoomID = HexUtil.getMD5(sb.toString());
        return "E"+makeRoomID.toUpperCase();
    }

    private String makeGroupChatRoomID(String appid, String userid) throws Exception{
        String groupMakeRoomID = HexUtil.getMD5(userid+System.currentTimeMillis());
        return "G"+groupMakeRoomID.toUpperCase();
    }
}
