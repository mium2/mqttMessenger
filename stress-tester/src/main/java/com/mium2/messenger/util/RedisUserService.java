package com.mium2.messenger.util;

import com.google.gson.Gson;
import com.mium2.messenger.util.bean.ChatRoomInfoBean;
import com.mium2.messenger.util.bean.UserInfoBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 9. 26..
 */
@Service
public class RedisUserService {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public final static String REDIS_CHATUSER_TABLE = "H_USER";  // 사용자아이디 : 사용자 정보
    public final static String REDIS_HP_TABLE = "H_HP_USERID";  // 핸드폰키: 사용자아이디
    public final static String REDIS_USERID_ROOMIDS_TABLE = "H_USERID_ROOMIDS"; // 사용자아이디 : [채팅방]
    public final static String REDIS_ROOMINFO_TABLE = "H_ROOMINFO"; //채팅방아이디 : 채팅방정보(채팅방아이디,대화참여자아이디s,핸드폰번호s,앱아이디)
    public final static String REDIS_ROOMID_SUBSCRUBE = "H_ROOMID_SUBSCRIBE";  // 채팅방아이디 : [사용자아이디s]
    public final static String REDIS_LOGINUSER_BROKERID = "H_LOGINUSER_BROKERID"; //LOGIN 아아디 : 할당된 브로커아이디
    public final static String REDIS_ROOMID_MSG = "L_ROOMID_";  // 채팅방아이디 : 발송된 메세지 ==> 채팅방별 지난대화리스트를 위해.
    private Gson gson = new Gson();

    @Autowired
    @Qualifier("masterRedisTemplate")
    private RedisTemplate<String,Object> masterRedisTemplate;//!!!주의!!! 쓰기/삭제 전용

    /**
     * Chat 유저 등록
     * @param userInfoBean
     */
    public void putUser(UserInfoBean userInfoBean) throws Exception{
        // userID를 키로 유저정보 Json Data 저장
        masterRedisTemplate.opsForHash().put(REDIS_CHATUSER_TABLE, userInfoBean.getUSERID(), JsonObjectConverter.getAsJSON(userInfoBean));
        // 핸드폰 번호를 키로 userID를 넣는다. 친구리스트 요청을 위해
        masterRedisTemplate.opsForHash().put(REDIS_HP_TABLE, userInfoBean.getHP_NUM(), userInfoBean.getUSERID());
    }

    /**
     * 로그인 성공한 아이디 저장
     * @param loginID
     * @throws Exception
     */
    public void putLoginID(String loginID,String brokerID) throws Exception{
        masterRedisTemplate.opsForHash().put(REDIS_LOGINUSER_BROKERID,loginID,brokerID);
    }

    /**
     * 챗팅방 만들기
     * @throws Exception
     */
    public void makeChatRoom(ChatRoomInfoBean chatRoomInfoBean, Set<String> inviteUserIDSet, Set<String> userIDs_BrokerIDs) throws Exception{
        // REDIS 키테이블에 채팅방 정보 저장
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        String nowDate = sdf.format(date);
        chatRoomInfoBean.setCREATED_DATE(nowDate);

        String jsonChatRoomInfo = JsonObjectConverter.getAsJSON(chatRoomInfoBean);
        masterRedisTemplate.opsForHash().put(REDIS_ROOMINFO_TABLE,chatRoomInfoBean.getROOMID(),jsonChatRoomInfo);
        // 대화방아이디:등록유저아이디들...
        masterRedisTemplate.opsForHash().put(REDIS_ROOMID_SUBSCRUBE,chatRoomInfoBean.getROOMID(),JsonObjectConverter.getAsJSON(userIDs_BrokerIDs));

        // 채팅방에 초대된 유저아이디 별로 해당 대화방 아이디를 등록한다. 이유는 클라이언트에서 채팅방리스트를 요청하므로.
        for(Object userIdObj : inviteUserIDSet) {
            String userID = userIdObj.toString();
            Object obj = masterRedisTemplate.opsForHash().get(REDIS_USERID_ROOMIDS_TABLE, userID);
            Set<String> chatRoomIdSet = new HashSet<String>();
            if(obj!=null){
                chatRoomIdSet = JsonObjectConverter.getObjectFromJSON(obj.toString(), HashSet.class);
            }
            chatRoomIdSet.add(chatRoomInfoBean.getROOMID());
            masterRedisTemplate.opsForHash().put(REDIS_USERID_ROOMIDS_TABLE,userID,JsonObjectConverter.getAsJSON(chatRoomIdSet));
        }
    }


    public HashSet<String> getSubscribeClientID(String roomid) throws Exception{

        HashSet<String> cliendIdSet = null;
        // MQTT서버를 통해 Subscribe를 한 경우
//        Object obj = redisTemplate.opsForHash().get(REDIS_SUBSCRIBE, topic);
        // 메신저 API서버를 통해 대화방을 생성한 경우.
        Object obj = masterRedisTemplate.opsForHash().get(REDIS_ROOMID_SUBSCRUBE,roomid);
        if (obj != null) {
            String clientIdSetJsonString = obj.toString();
            cliendIdSet = gson.fromJson(clientIdSetJsonString, HashSet.class);
        }

        return cliendIdSet;
    }

    public void putTest(){
        long startTime = System.currentTimeMillis();
        for(int i=0; i<100000; i++){
            String putString =i+"_abcdefg fjdaklfjdlasfjsa fjdsaklfjsadlfjsaflksajflsa fasjflkas jfl";

//            UserInfoBean userInfoBean = new UserInfoBean();
//            userInfoBean.setUSERID("TEST" + i);
//            userInfoBean.setNICKNAME("TEST" + i);
//            userInfoBean.setAPPID("com.uracle.test");
//            userInfoBean.setDEVICEID("DEVICEID" + i);
//            userInfoBean.setHP_NUM("hp" + i);
//            userInfoBean.setBROKER_ID("brokerid");
//            userInfoBean.setNICKNAME("NICNAME" + i);
//            userInfoBean.setMPSN("");
//            userInfoBean.setPASSWORD("be4098de597fd3f17a90c6b998a87c453bd302af40bd65230c65b0bbf5979abc");
//            userInfoBean.setPUSH_SERVER("");
//            userInfoBean.setPUSH_TOKEN("");

//            String putString = null;
            try {
//                putString = JsonObjectConverter.getAsJSON(userInfoBean);
                masterRedisTemplate.opsForHash().put("PUTTEST",""+i,putString);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        System.out.println("##### 처리시간 : "+ (System.currentTimeMillis()-startTime));
    }

}
