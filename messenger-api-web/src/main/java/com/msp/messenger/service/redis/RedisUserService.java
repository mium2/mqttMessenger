package com.msp.messenger.service.redis;

import com.msp.messenger.bean.ChatRoomInfoBean;
import com.msp.messenger.bean.MakeRoomBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.util.JsonObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 13..
 */
@Service
public class RedisUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public final static String REDIS_CHATUSER_TABLE = ":USER";
    public final static String REDIS_HP_TABLE = ":HP_USERID";
    public final static String REDIS_USERID_ROOMIDS_TABLE = ":USERID_ROOMIDS";
    public final static String REDIS_ROOMINFO_TABLE = ":ROOMINFO";
    public final static String REDIS_ROOMID_SUBSCRUBE = ":ROOMID_SUBSCRIBE";  // 메신저서버가 발송할때 참조하는 키테이블
    public final static String REDIS_USER_BROKERID = ":USER_BROKERID";

    @Value("${MAX_BROKER_ALLOCATE_USER:500000}")
    private String MAX_BROKER_ALLOCATE_USER;

    @Value("${BROKER_GROUP_YN:N}")
    private String BROKER_GROUP_YN;

    @Autowired
    @Qualifier("masterRedisTemplate")
    private RedisTemplate<String,Object> masterRedisTemplate;//!!!주의!!! 쓰기/삭제 전용

    @Autowired
    @Qualifier("slaveRedisTemplate")
    private RedisTemplate<String,Object> slaveRedisTemplate;//!!!주의!!! 읽기 전용

    /**
     * Redis Chat 유저가입수
     */
    public Map<String,String> printAllPushUserCount(String appid){
        //전체유저
        long totalSize = slaveRedisTemplate.opsForHash().size(appid + REDIS_CHATUSER_TABLE);
        System.out.println("Total Total PushUser:" + totalSize);

        Map<String,String> returnMap = new HashMap<String, String>();
        returnMap.put("TOTAL", "" + totalSize);

        return returnMap;
    }

    /**
     * Redis 전체삭제
     * @param appid
     */
    public void removeAll(String appid){
        if(appid==null){
            appid = "com.uracle.push.test";
        }
        masterRedisTemplate.delete(appid + REDIS_CHATUSER_TABLE);
        masterRedisTemplate.delete(appid + REDIS_HP_TABLE);
    }

    /**
     * 유저아이디를 이용하여 푸시유저정보 조회
     * @param userID
     * @return
     */
    public UserInfoBean getUserInfo(String appid, String userID) {
        Object obj = slaveRedisTemplate.opsForHash().get(appid+REDIS_CHATUSER_TABLE, userID);
        UserInfoBean getUserInfo = null;
        if(obj!=null){
            getUserInfo = JsonObjectConverter.getObjectFromJSON(obj.toString(), UserInfoBean.class);
        }
        return getUserInfo;
    }

    /**
     * userID set을 이용한 푸시유저정보 리스트 조회
     * @param appid
     * @param userID
     * @return
     */
    public UserInfoBean getUserInfoFromUserid(String appid, String userID) {
        UserInfoBean subscribeInfoBean = null;
        Object userInfoJson = slaveRedisTemplate.opsForHash().get(appid + RedisUserService.REDIS_CHATUSER_TABLE, userID);
        if(userInfoJson!=null) {
            try{
                subscribeInfoBean = JsonObjectConverter.getObjectFromJSON(userInfoJson.toString(), UserInfoBean.class);
            }catch(Exception e){}
        }
        return subscribeInfoBean;
    }

    /**
     * userid set을 이용한 푸시유저정보 리스트 조회
     * @param appid
     * @param useridSet
     * @return
     */
    public List<UserInfoBean> multiGetUserInfoFromUserIdset(String appid, Collection<Object> useridSet) {
        List<UserInfoBean> basicPushUserBeans = new ArrayList<UserInfoBean>();
        List<Object> objs = slaveRedisTemplate.opsForHash().multiGet(appid + REDIS_CHATUSER_TABLE, useridSet);
        for(Object obj : objs){
            if(obj!=null){
                try{
                    UserInfoBean basicPushUserBean = JsonObjectConverter.getObjectFromJSON(obj.toString(),UserInfoBean.class);
                    basicPushUserBeans.add(basicPushUserBean);
                }catch (Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
        }

        return basicPushUserBeans;
    }

    /**
     * 핸드폰번호 set을 이용한 유저정보 리스트 조회
     * @param appid
     * @param hpNumSet
     * @return
     */
    public List<UserInfoBean> multiGetUserInfoFromHpNumset(String appid, Collection<Object> hpNumSet) {
        List<UserInfoBean> userInfoBeans = new ArrayList<UserInfoBean>();
        List<Object> userIDList = slaveRedisTemplate.opsForHash().multiGet(appid + REDIS_HP_TABLE, hpNumSet);

        Set<Object> userIdSet = new HashSet<Object>();
        for(Object userID : userIDList){
            if(userID!=null){
                try {
                    userIdSet.add(userID);
                }catch (Exception e){}
            }
        }
        if(userIdSet.size()>0){
            userInfoBeans = multiGetUserInfoFromUserIdset(appid,userIdSet);
        }
        return userInfoBeans;
    }

    /**
     * Chat 유저 등록
     * @param userInfoBean
     */
    public void putUser(UserInfoBean userInfoBean) throws Exception{
        Object userIdObj = getUseridFromHpNum(userInfoBean.getAPPID(),userInfoBean.getHP_NUM());
        if(userIdObj!=null){
            // 기존 핸드폰 번호로 등록된 사용자는 삭제한다. 핸드폰번호를 바꾸고 다른 사용자가 해당 핸드폰을 사용한 경우이므로.
            masterRedisTemplate.opsForHash().delete(userInfoBean.getAPPID() + REDIS_CHATUSER_TABLE,userIdObj.toString());
        }
        // userID를 키로 유저정보 Json Data 저장
        masterRedisTemplate.opsForHash().put(userInfoBean.getAPPID() + REDIS_CHATUSER_TABLE, userInfoBean.getUSERID(), JsonObjectConverter.getAsJSON(userInfoBean));
        // 핸드폰 번호를 키로 userID를 넣는다. 친구리스트 요청을 위해
        masterRedisTemplate.opsForHash().put(userInfoBean.getAPPID() + REDIS_HP_TABLE, userInfoBean.getHP_NUM(), userInfoBean.getUSERID());
    }

    /**
     * Chat 유저 수정
     * @param userInfoBean
     * @param oldHP_NUM
     */
    public void editUser(UserInfoBean userInfoBean, String oldHP_NUM) throws Exception{
        // userID를 키로 유저정보 Json Data 저장
        masterRedisTemplate.opsForHash().put(userInfoBean.getAPPID() + REDIS_CHATUSER_TABLE, userInfoBean.getUSERID(), JsonObjectConverter.getAsJSON(userInfoBean));
        // 핸드폰 번호를 키로 userID를 넣는다. 친구리스트 요청을 위해
        if(!userInfoBean.getHP_NUM().equals(oldHP_NUM)){
            //이전 핸드폰번호로 맵핑되어있는 USERID 삭제한다.
            masterRedisTemplate.opsForHash().delete(userInfoBean.getAPPID() + REDIS_HP_TABLE, oldHP_NUM);
        }
        masterRedisTemplate.opsForHash().put(userInfoBean.getAPPID() + REDIS_HP_TABLE, userInfoBean.getHP_NUM(), userInfoBean.getUSERID());
    }

    /**
     * 브로커서버아이디가 변경되었을 경우 구독정보의 해당아이디가 할당된 브로커서버아이디를 변경한다.
     * @param appid
     * @param userid
     * @param oldBrokerid
     * @param newBrokerid
     */
    public void chgSubscribeServerID(String appid,String userid,String oldBrokerid,String newBrokerid){
        Map<String,String> multiPutSubscriber = new HashMap<String, String>();
        Object jsonStringObj = slaveRedisTemplate.opsForHash().get(appid+REDIS_USERID_ROOMIDS_TABLE,userid);
        if(jsonStringObj!=null){
            List<Object> chatRoomIds = JsonObjectConverter.getObjectFromJSON(jsonStringObj.toString(), List.class);
            List<Object> subscribers = slaveRedisTemplate.opsForHash().multiGet(appid+REDIS_ROOMID_SUBSCRUBE,chatRoomIds);
            for(int i=0; i<subscribers.size(); i++){
                if(subscribers.get(i)!=null){
                    HashSet<String> subscriberSet = JsonObjectConverter.getObjectFromJSON(subscribers.get(i).toString(),HashSet.class);
                    subscriberSet.remove(userid+"|"+oldBrokerid);
                    subscriberSet.add(userid + "|" + newBrokerid);
                    try {
                        multiPutSubscriber.put(chatRoomIds.get(i).toString(), JsonObjectConverter.getAsJSON(subscriberSet));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
        masterRedisTemplate.opsForHash().putAll(appid + REDIS_ROOMID_SUBSCRUBE, multiPutSubscriber);
    }

    public void putUserIDBrokerID(String appid,String clientID, String brokerID){
        masterRedisTemplate.opsForHash().put(appid + REDIS_USER_BROKERID, clientID, brokerID);
    }

    public void removeUser(UserInfoBean userInfoBean) throws Exception {
        //1. 유저 삭제
        masterRedisTemplate.opsForHash().delete(userInfoBean.getAPPID() + REDIS_CHATUSER_TABLE, userInfoBean.getUSERID());
        //2. HP 삭제
        masterRedisTemplate.opsForHash().delete(userInfoBean.getAPPID() + REDIS_HP_TABLE, userInfoBean.getHP_NUM());
    }

    public String isExistChatRoomTopic(MakeRoomBean makeRoomBean, int memberCnt){
        String returnRoomID = makeRoomBean.getROOMID();
        // TODO : 같은 멤버로 존재는 하나 방을 나간 사용자가 있다면 방아이디를 새롭게 만들어야 할 필요가 있음.
        Object obj = slaveRedisTemplate.opsForHash().get(makeRoomBean.getAPPID()+REDIS_ROOMINFO_TABLE,makeRoomBean.getROOMID());
        if(obj!=null){
            ChatRoomInfoBean chatRoomInfoBean = JsonObjectConverter.getObjectFromJSON(obj.toString(),ChatRoomInfoBean.class);
            if(chatRoomInfoBean.getUSERIDS().size()!=memberCnt){
                // 방을 나간 회원이 있으므로 신규 방아이디를 새로운 아이디로 생성하고 만들어줌.
                String nowMiliTimeStr = System.currentTimeMillis()+"";
                long firstTime = Long.parseLong(nowMiliTimeStr.substring(0,10));
                long secondTime = Long.parseLong(nowMiliTimeStr.substring(10));
                long orgRoomID = Long.parseLong(makeRoomBean.getROOMID());
                long sumRoomID = firstTime+secondTime+orgRoomID;
                returnRoomID = "R"+sumRoomID;
            }
        }else{
            returnRoomID = null;
        }
        return returnRoomID;
    }

    public void makeChatRoom(MakeRoomBean makeRoomBean,List<Object> userIDs,List<String> userIDs_BrokerIDs, List<Object> hpNums, TreeSet<Object> inviteUserIDTreeSet) throws Exception{
        // REDIS 키테이블에 채팅방 정보 저장
        ChatRoomInfoBean chatRoomInfoBean = new ChatRoomInfoBean();
        chatRoomInfoBean.setAPPID(makeRoomBean.getAPPID());
        chatRoomInfoBean.setROOMID(makeRoomBean.getROOMID());
        chatRoomInfoBean.setROOM_OWNER(makeRoomBean.getUSERID());
        chatRoomInfoBean.setUSERIDS(userIDs);
        chatRoomInfoBean.setHPNUMS(hpNums);
        String jsonChatRoomInfo = JsonObjectConverter.getAsJSON(chatRoomInfoBean);
        masterRedisTemplate.opsForHash().put(makeRoomBean.getAPPID()+REDIS_ROOMINFO_TABLE,makeRoomBean.getROOMID(),jsonChatRoomInfo);
        // 대화방아이디:등록유저아이디들...
        masterRedisTemplate.opsForHash().put(makeRoomBean.getAPPID()+REDIS_ROOMID_SUBSCRUBE,makeRoomBean.getROOMID(),JsonObjectConverter.getAsJSON(userIDs_BrokerIDs));

        // 채팅방에 초대된 유저아이디 별로 해당 대화방 아이디를 등록한다. 이유는 클라이언트에서 채팅방리스트를 요청하므로.
        for(Object userIdObj : inviteUserIDTreeSet) {
            String userID = userIdObj.toString();
            Object obj = slaveRedisTemplate.opsForHash().get(makeRoomBean.getAPPID() + REDIS_USERID_ROOMIDS_TABLE, userID);
            Set<String> chatRoomIdSet = new HashSet<String>();
            if(obj!=null){
                chatRoomIdSet = JsonObjectConverter.getObjectFromJSON(obj.toString(), HashSet.class);
            }
            chatRoomIdSet.add(makeRoomBean.getROOMID());
            masterRedisTemplate.opsForHash().put(makeRoomBean.getAPPID()+REDIS_USERID_ROOMIDS_TABLE,userID,JsonObjectConverter.getAsJSON(chatRoomIdSet));
        }
    }

    public List<ChatRoomInfoBean> getChatRoomList(String appid, String userid){
        List<ChatRoomInfoBean> chatRoomInfoList = new ArrayList<ChatRoomInfoBean>();
        Object obj = slaveRedisTemplate.opsForHash().get(appid + REDIS_USERID_ROOMIDS_TABLE, userid);
        Set<Object> chatRoomIdSet = new HashSet<Object>();
        if(obj!=null){
            chatRoomIdSet = JsonObjectConverter.getObjectFromJSON(obj.toString(), HashSet.class);
        }

        // 챗팅방아이디들로 챗팅방정보JSON스트링리스트를 가져온다.
        if(chatRoomIdSet.size()>0){
            List<Object> chatRoomInfoJsonList = slaveRedisTemplate.opsForHash().multiGet(appid + REDIS_ROOMINFO_TABLE, chatRoomIdSet);
            for(Object chatRoomInfoJsonString : chatRoomInfoJsonList){
                ChatRoomInfoBean chatRoomInfoBean = JsonObjectConverter.getObjectFromJSON(chatRoomInfoJsonString.toString(),ChatRoomInfoBean.class);
                chatRoomInfoList.add(chatRoomInfoBean);
            }
        }
        return chatRoomInfoList;
    }

    public ChatRoomInfoBean getChatRoomDetailInfo(String appid,String roomid){
        ChatRoomInfoBean chatRoomInfoBean = null;

        Object obj = slaveRedisTemplate.opsForHash().get(appid+REDIS_ROOMINFO_TABLE,roomid);
        if(obj!=null){
            chatRoomInfoBean = JsonObjectConverter.getObjectFromJSON(obj.toString(),ChatRoomInfoBean.class);
        }
        return chatRoomInfoBean;
    }

    public void forceRemoveChatRoom(String appid, String roomid){
        //STEP1. ROOMID_SUBSCRIBE에서 속해있는 해당 유저들을 찾아내 USERID_ROOMIDS 키테이블에서 대화방 삭제
        //STEP2. ROOMINFO 키테이블에서 대화방삭제
        //STEP3. ROOMID_SUBSCRIBE 키테이블에서 대화방 삭제
        Object subscribersObj = slaveRedisTemplate.opsForHash().get(appid+REDIS_ROOMID_SUBSCRUBE,roomid);
        List<Object> multiUseridList = new ArrayList<Object>();
        if(subscribersObj!=null){
            HashSet<String> subscribersSet = JsonObjectConverter.getObjectFromJSON(subscribersObj.toString(),HashSet.class);
            for(String subscriber : subscribersSet){
                String[] subscriberArr = subscriber.split("\\|");
                String userID = subscriberArr[0];
                multiUseridList.add(userID);
            }

            //USERID_ROOMIDS 키테이블에서 대화방 삭제
            List<Object> roomSetList = slaveRedisTemplate.opsForHash().multiGet(appid+REDIS_USERID_ROOMIDS_TABLE,multiUseridList);
            for(int i=0; i<roomSetList.size(); i++){
                Object obj = roomSetList.get(i);
                if(obj!=null){
                    HashSet<String> roomIdSet = JsonObjectConverter.getObjectFromJSON(obj.toString(),HashSet.class);
                    roomIdSet.remove(roomid);
                    try {
                        masterRedisTemplate.opsForHash().put(appid + REDIS_USERID_ROOMIDS_TABLE, multiUseridList.get(i).toString(), JsonObjectConverter.getAsJSON(roomIdSet));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }

            masterRedisTemplate.opsForHash().delete(appid+REDIS_ROOMINFO_TABLE,roomid);

            masterRedisTemplate.opsForHash().delete(appid+REDIS_ROOMID_SUBSCRUBE,roomid);
        }

    }

    public void getOutChatRoom(String appid, String userid, String roomid) throws Exception{
        // TODO : 1.챗팅방 정보에서 유저아이디리스트, 핸폰리스트에서 삭제 처리. 2.유저아이디_챗팅룸아이디 리스트에서 해당 챗팅룸 아이디 삭제. 3.챗팅방아이디에 유저아이디 리스트에서 해당 아이디 삭제처리
        // STEP1 : 챗팅방정보에서 삭제.
        // 만약 한명만 남는 경우는 어떻게 처리 해야 되나? 채팅방을 눌렀을때 서버로 부터 상세정보를 받고 그때 대화상대가 없다는 표시를 하도록 유도한다.
        Object chatRoomJsonObj = slaveRedisTemplate.opsForHash().get(appid+REDIS_ROOMINFO_TABLE,roomid);
        if(chatRoomJsonObj!=null){
            ChatRoomInfoBean chatRoomInfoBean = JsonObjectConverter.getObjectFromJSON(chatRoomJsonObj.toString(),ChatRoomInfoBean.class);
            List<Object> userids = chatRoomInfoBean.getUSERIDS();
            int removeIndex = -1;
            for(int i=0; i<userids.size(); i++){
                if(userids.get(i).toString().equals(userid)){
                    userids.remove(i);
                    removeIndex = i;
                    break;
                }
            }
            List<Object> hpNums = chatRoomInfoBean.getHPNUMS();
            // 채팅방에서 유저가 삭제된경우
            if(removeIndex>-1){
                hpNums.remove(removeIndex);
                // 이곳에 한 이유는 성능을 위해 유저아이디가 삭제된 경우에만 재 저장로직을 태움.
                chatRoomInfoBean.setUSERIDS(userids);
                chatRoomInfoBean.setHPNUMS(hpNums);
                masterRedisTemplate.opsForHash().put(appid+REDIS_ROOMINFO_TABLE,roomid,JsonObjectConverter.getAsJSON(chatRoomInfoBean));
            }
        }
        // STEP 2 : 채팅방아이디 구독자정보 리스트 키테이블에서 해당 유저 삭제
        Object subscribeObj = slaveRedisTemplate.opsForHash().get(appid+REDIS_ROOMID_SUBSCRUBE,roomid);
        if(subscribeObj!=null){
            List<String> subscribeList = JsonObjectConverter.getObjectFromJSON(subscribeObj.toString(),List.class);
            for(String userid_brokerid : subscribeList){
                if(userid_brokerid.startsWith(userid+"|")){
                    subscribeList.remove(userid_brokerid);
                    break;
                }
            }
            masterRedisTemplate.opsForHash().put(appid+REDIS_ROOMID_SUBSCRUBE,roomid,JsonObjectConverter.getAsJSON(subscribeList));
        }

        // STEP 3. 유저아이디키에 대화방아이디 리스트 키테이블에서 해당 대화방 삭제.
        // 채팅방에 초대된 유저아이디 별로 해당 대화방 아이디를 등록한다. 이유는 클라이언트에서 채팅방리스트를 요청하므로.
        Object roomIDsSetObj =slaveRedisTemplate.opsForHash().get(appid+REDIS_USERID_ROOMIDS_TABLE,userid);
        if(roomIDsSetObj!=null){
            Set<String> roomIdSet = JsonObjectConverter.getObjectFromJSON(roomIDsSetObj.toString(), HashSet.class);
            if(roomIdSet.remove(roomid)){
                masterRedisTemplate.opsForHash().put(appid+REDIS_USERID_ROOMIDS_TABLE,userid,JsonObjectConverter.getAsJSON(roomIdSet));
            }
        }
    }

    public boolean isExistHpNum(String appid, String hpNum){
        Boolean returnVal = slaveRedisTemplate.opsForHash().hasKey(appid + REDIS_HP_TABLE, hpNum);
        return returnVal;
    }

    public Object getUseridFromHpNum(String appid, String hpNum) {
        return slaveRedisTemplate.opsForHash().get(appid + REDIS_HP_TABLE, hpNum);
    }
}
