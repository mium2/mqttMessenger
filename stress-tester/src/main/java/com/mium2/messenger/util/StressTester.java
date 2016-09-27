package com.mium2.messenger.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.mium2.messenger.util.bean.ChatRoomInfoBean;
import com.mium2.messenger.util.bean.UserInfoBean;
import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.internal.MemoryPersistence;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 22..
 */
public class StressTester {
    private final static Logger logger = LoggerFactory.getLogger("com.mium2.messenger.util");

    public static ApplicationContext ctx = null;
    public final static String SERVER_CONF_FILE = "./config/config.xml";
    public final static String LOG_CONF_FILE = "./config/logback.xml";
    private final static String VERSION = "1.0.0";

    private Map<String,MqttClient> mqttClientMap = new HashMap<String, MqttClient>();
    private Map<String,String> roomIDSenderMap = new HashMap<String, String>();
    private RedisUserService redisUserService;

    private int createUserCnt = 0;
    private String testUserPrefix = "";
    private int createRoomCnt = 0;
    private String testRoomPrefix = "";
    private String brokerID = "";
    private String appid = "com.uracle.test";
    private String hphonePrefix = "010000";
    private String brokerIpPort = "";

    public static void main(String[] args){
        System.out.println("Server started, version " + VERSION);
        logger.info("########################################################");
        logger.info("####   MQTT-MESSENGER-SERVER "+ VERSION +"Starting~~!    ####");
        logger.info("########################################################");

        //Logger 설정
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(LOG_CONF_FILE);
        } catch (JoranException je) {
            logger.error(je.getMessage());
            return;
        }
        //브로커서버 config 설정 로드
        try {
            ConfigLoader.Load(SERVER_CONF_FILE);
        }catch(ConfigurationException e) {
            logger.error(e.getMessage());
            return;
        }

        ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);  //스프링 Config 호출

        StressTester stressTester = new StressTester();

        stressTester.init();

//        // 메신저 클라이언트 브로커 연결 세션 만들기
//        stressTester.makeConnectChannels();
//
//        // 부하테스트를 위한 자동 메세지 발송
//        for(int i=0; i<1; i++){
//            AutoMsgSendThread autoMsgSendThread = new AutoMsgSendThread("Thread-"+i,stressTester);
//            autoMsgSendThread.start();
//        }

        stressTester.putRedisTest();
    }

    private void putRedisTest(){
        redisUserService.putTest();
    }

    private void init(){
        // 유저등록
        redisUserService = (RedisUserService)ctx.getBean("redisUserService");
        createUserCnt = ConfigLoader.getIntProperty(ConfigLoader.CLIENT_MAKE_CNT);
        testUserPrefix = ConfigLoader.getProperty(ConfigLoader.CLIENT_PREFIX);
        createRoomCnt = ConfigLoader.getIntProperty(ConfigLoader.CHATROOM_MAKE_CNT);
        testRoomPrefix = ConfigLoader.getProperty(ConfigLoader.CHATROOM_PREFIX);
        brokerID = ConfigLoader.getProperty(ConfigLoader.BROKERID);
        appid = "com.uracle.test";
        hphonePrefix = "010000";
        brokerIpPort = ConfigLoader.getProperty(ConfigLoader.BROKERIP_PORT);

//        createChatUser();
//        loginProcess();
//        createChatRoom();
    }

    /**
     * 챗팅유저 생성
     */
    private void createChatUser(){
        for(int i=1; i<=createUserCnt; i++) {
            UserInfoBean userInfoBean = new UserInfoBean();
            userInfoBean.setUSERID(testUserPrefix+i);
            userInfoBean.setNICKNAME(testUserPrefix + i);
            userInfoBean.setAPPID(appid);
            userInfoBean.setDEVICEID("DEVICEID" + i);
            userInfoBean.setHP_NUM(hphonePrefix + i);
            userInfoBean.setBROKER_ID(brokerID);
            userInfoBean.setNICKNAME("NICNAME" + i);
            userInfoBean.setMPSN("");
            userInfoBean.setPASSWORD("be4098de597fd3f17a90c6b998a87c453bd302af40bd65230c65b0bbf5979abc");
            userInfoBean.setPUSH_SERVER("");
            userInfoBean.setPUSH_TOKEN("");
            try {
                redisUserService.putUser(userInfoBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 로그인
     */
    private void loginProcess(){
        for(int i=1; i<=createUserCnt; i++) {
            try {
                redisUserService.putLoginID(testUserPrefix+i, brokerID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 챗팅방 생성
     */
    private void createChatRoom(){
        Set<String> inviteUserIdset = new HashSet<String>();
        Set<String> userID_brokerIDset = new HashSet<String>();
        List<Object> inviteUserIdlist = new ArrayList<Object>();
        List<Object> hpNums = new ArrayList<Object>();
        String ownId = "";
        // 그룹 채팅방 만들기
        for(int i=1; i<=createRoomCnt; i++){
            int inviteUserCnt = (int) (Math.random() * 9)+2; // 2~10 사이의 램덤 수
            for(int j=0; j<inviteUserCnt; j++){
                int userNum = (int) (Math.random() * createUserCnt)+1; //
                String inviteUserID = testUserPrefix+userNum;
                String hphoneNum = hphonePrefix+userNum;
                if(!inviteUserIdset.contains(inviteUserID)) {
                    inviteUserIdlist.add(inviteUserID);
                    userID_brokerIDset.add(inviteUserID + "|" + brokerID);
                    hpNums.add(hphoneNum);
                    inviteUserIdset.add(inviteUserID);
                }
                if(j==0){
                    ownId = inviteUserID;
                }
            }

            ChatRoomInfoBean chatRoomInfoBean = new ChatRoomInfoBean();
            chatRoomInfoBean.setAPPID(appid);
            chatRoomInfoBean.setROOMID(testRoomPrefix + i);
            chatRoomInfoBean.setROOM_NAME(testRoomPrefix + i);
            chatRoomInfoBean.setROOM_OWNER(ownId);
            chatRoomInfoBean.setUSERIDS(inviteUserIdlist);
            chatRoomInfoBean.setHPNUMS(hpNums);
            try {
                redisUserService.makeChatRoom(chatRoomInfoBean,inviteUserIdset,userID_brokerIDset);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //발송자아이디를 구하기 위해 방에 초대된 사용자수의 랜덤한 인덱스를 추출한다.
            int sendRandomIndex = (int)(Math.random() * inviteUserIdlist.size());
            String senderID = (String)inviteUserIdlist.get(sendRandomIndex);
            //대화방에 발송자아이디를 랜덤하게 넣는다.
            roomIDSenderMap.put(chatRoomInfoBean.getROOMID(),senderID);

            inviteUserIdset.clear();
            userID_brokerIDset.clear();
            inviteUserIdlist.clear();
            hpNums.clear();
        }
    }

    /**
     * 생성한 클라이언트 유저 컨넥션한다.
     * @return
     */
    private void makeConnectChannels(){
        MqttClient mConnection = null;
        try {
            String BROKER_URL = "tcp://"+brokerIpPort;
            int MQTT_KEEP_ALIVE = 120;
            for(int i=1; i<=createUserCnt; i++) {
                String clientID = testUserPrefix+i;
                mConnection = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
                mConnection.setCallback(new MessengerCallback());
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setCleanSession(false);
                mqttConnectOptions.setConnectionTimeout(10);
                mqttConnectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE);
                mConnection.connect(mqttConnectOptions);
                mqttClientMap.put(clientID, mConnection);
            }
        } catch (Exception e) {
            if (e instanceof MqttException) {
                //서버의 ConnectAck 에러번호가 6일때는 다른 브로커서버로 접속하라고 구현
                if (((MqttException) e).getReasonCode() == 6) {
                    logger.error("접속할 다른 서버를 알아내는 로직 구현해야함");
                }
            }else {
                logger.error("#####에러:" + e.getMessage());
            }
        }

        logger.info("#### makeChannelSize :"+mqttClientMap.size());
    }

    private void randomMsgSend(){
        long statTime = System.currentTimeMillis();
//        mqttClient.publish("A_TOPIC", msgMessage.toString().getBytes(), 1, false, j);
        long endTime = System.currentTimeMillis();

        logger.debug("경과시간:" + (endTime-statTime));
    }


    public RedisUserService getRedisUserService() {
        return redisUserService;
    }

    public void setRedisUserService(RedisUserService redisUserService) {
        this.redisUserService = redisUserService;
    }

    public MqttClient getMqttClient(String clientID){
        return mqttClientMap.get(clientID);
    }

    public String getRandomRoomIDSenderIdMap() {
        int randomRoomIndex = (int)(Math.random() * createUserCnt)+1;
        String senderid = testUserPrefix+randomRoomIndex;
        String roomid = roomIDSenderMap.get(senderid);

        return roomid+"|"+senderid;
    }
}
