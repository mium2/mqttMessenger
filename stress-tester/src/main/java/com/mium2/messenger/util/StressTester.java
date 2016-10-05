package com.mium2.messenger.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.mium2.messenger.util.bean.ChatRoomInfoBean;
import com.mium2.messenger.util.bean.UserInfoBean;
import com.mium2.messenger.util.client.BrokerConnectManager;
import com.mium2.messenger.util.client.MessageWorker;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 22..
 */
public class StressTester {
    private final static Logger logger = LoggerFactory.getLogger("com.mium2.messenger.util");

    private static StressTester instance = null;
    public static ApplicationContext ctx = null;
    public final static String SERVER_CONF_FILE = "./config/config.xml";
    public final static String LOG_CONF_FILE = "./config/logback.xml";
    private final static String VERSION = "1.0.0";

    private Map<String,String> roomIDSenderMap = new HashMap<String, String>();
    private RedisUserService redisUserService;
    private BrokerConnectManager brokerConnectManager;

    private int createUserStartPos =1;
    private int createUserEndPos = 100;
    private int createUserCnt = 0;
    private String clientUserPrefix = "";
    private int createRoomStartPos = 1;
    private int createRoomEndPos = 2;
    private int createRoomCnt = 0;
    private String rooomIdPrefix = "";
    private String brokerID = "";
    private String appid = "com.uracle.test";
    private String hphonePrefix = "010000";
    private String brokerIpPort = "";
    private long broker_connect_sleep = 0;

    private StressTester(){}

    public static StressTester getInstance(){
        if(instance==null){
            instance = new StressTester();
        }
        return instance;
    }

    public static void main(String[] args){
        System.out.println("Server started, version " + VERSION);
        logger.info("########################################################");
        logger.info("####   STRESS MESSENGER TESTER "+ VERSION +"Starting~~!    ####");
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
        logger.info("########################################################");
        logger.info("####   Logger Loader Success~~!    ####");
        logger.info("########################################################");
        //브로커서버 config 설정 로드
        try {
            ConfigLoader.Load(SERVER_CONF_FILE);
        }catch(ConfigurationException e) {
            logger.error(e.getMessage());
            return;
        }
        logger.info("########################################################");
        logger.info("####   Config Loader Success~~!    ####");
        logger.info("########################################################");
        ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);  //스프링 Config 호출

        // 유저가입,로그인처리,대화방 개설
        StressTester.getInstance().init();


        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        for(int i=0; i<3  ; i ++) {
            threadPool.execute( new MessageWorker() );
        }
        // 메신저 클라이언트 브로커 연결 세션 만들기
        StressTester.getInstance().makeConnectChannels();

    }

    private void putRedisTest(){
        redisUserService.putTest();
    }

    private void init(){
        // 유저등록
        redisUserService = (RedisUserService)ctx.getBean("redisUserService");
        createUserStartPos = ConfigLoader.getIntProperty(ConfigLoader.CLIENT_START_POS);
        createUserEndPos = ConfigLoader.getIntProperty(ConfigLoader.CLIENT_END_POS);
        createUserCnt = createUserEndPos-createUserStartPos+1;
        clientUserPrefix = ConfigLoader.getProperty(ConfigLoader.CLIENT_PREFIX);

        createRoomStartPos = ConfigLoader.getIntProperty(ConfigLoader.CHATROOM_START_POS);
        createRoomEndPos = ConfigLoader.getIntProperty(ConfigLoader.CHATROOM_END_POS);
        createRoomCnt = createRoomEndPos-createRoomStartPos+1;
        rooomIdPrefix = ConfigLoader.getProperty(ConfigLoader.CHATROOM_PREFIX);
        brokerID = ConfigLoader.getProperty(ConfigLoader.BROKERID);
        appid = "com.uracle.test";
        hphonePrefix = "010000";
        brokerIpPort = ConfigLoader.getProperty(ConfigLoader.BROKERIP_PORT);

        broker_connect_sleep = ConfigLoader.getLongProperty(ConfigLoader.CONNECT_SLEEP_INTERVAL);

        logger.info("### CREATE USER CNT : {}    CREATE ROOM CNT: {}" , createUserCnt, createRoomCnt);

        createChatUser();
        loginProcess();
        createChatRoom();
    }

    /**
     * 챗팅유저 생성
     */
    private void createChatUser(){
        for(int i=createUserStartPos; i<=createUserEndPos; i++) {
            UserInfoBean userInfoBean = new UserInfoBean();
            userInfoBean.setUSERID(clientUserPrefix+i);
            userInfoBean.setNICKNAME(clientUserPrefix + i);
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
        for(int i=createUserStartPos; i<=createUserEndPos; i++) {
            try {
                redisUserService.putLoginID(clientUserPrefix+i, brokerID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 대화방 생성
     */
    private void createChatRoom(){
        Set<String> inviteUserIdset = new HashSet<String>();
        Set<String> userID_brokerIDset = new HashSet<String>();
        List<Object> inviteUserIdlist = new ArrayList<Object>();
        List<Object> hpNums = new ArrayList<Object>();
        String ownId = "";
        // 대화방 만들기
        for(int i=createRoomStartPos; i<=createRoomEndPos; i++){
            int inviteUserCnt = (int) (Math.random() * 9)+2; // 2~10 사이의 램덤 수
            for(int j=0; j<inviteUserCnt; j++){
                int inviteUserRandomNum = ((int)(Math.random() * createUserEndPos)-createUserStartPos)+1; // startPos ~ endPos 사이에 랜덤숫자
                if(inviteUserRandomNum<createUserStartPos){
                    inviteUserRandomNum = createUserStartPos;
                }
                String inviteUserID = clientUserPrefix+(inviteUserRandomNum+1);

                String hphoneNum = hphonePrefix+inviteUserRandomNum;
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
            chatRoomInfoBean.setROOMID(rooomIdPrefix + i);
            chatRoomInfoBean.setROOM_NAME(rooomIdPrefix + i);
            chatRoomInfoBean.setROOM_OWNER(ownId);
            chatRoomInfoBean.setUSERIDS(inviteUserIdlist);
            chatRoomInfoBean.setHPNUMS(hpNums);
            try {
                redisUserService.makeChatRoom(chatRoomInfoBean,inviteUserIdset,userID_brokerIDset);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //발송자아이디를 구하기 위해 방에 초대된 사용자수의 랜덤한 인덱스번호를 추출한다.
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
        String[] brokerIpPortArr = brokerIpPort.split(":");
        String brokerIP = brokerIpPortArr[0];
        int brokerPort = Integer.parseInt(brokerIpPortArr[1]);

        brokerConnectManager = BrokerConnectManager.getInstance();
        brokerConnectManager.init(brokerIP,brokerPort,clientUserPrefix,createUserStartPos,createUserEndPos);
        brokerConnectManager.reqAllConnection();
    }


    public RedisUserService getRedisUserService() {
        return redisUserService;
    }

    public void setRedisUserService(RedisUserService redisUserService) {
        this.redisUserService = redisUserService;
    }


    /**
     * 만들어진 대화방 중 랜덤하게 발송할 대화방 아이디와 대화방에 메세지를 발송할 아이디를 리턴해주는 함수.
     * @return
     */
    public String getRandomRoomIDSenderIdMap() {
        int randomRoomIndex = (int)(Math.random() * createRoomCnt)+1;
        String randomRoomID = rooomIdPrefix+randomRoomIndex;
        String senderid = roomIDSenderMap.get(randomRoomID);

        return randomRoomID+"|"+senderid;
    }
}
