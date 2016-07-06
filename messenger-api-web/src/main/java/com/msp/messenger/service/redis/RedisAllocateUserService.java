package com.msp.messenger.service.redis;

import com.msp.messenger.bean.BrokerConInfoBean;
import com.msp.messenger.bean.ServerInfoBean;
import com.msp.messenger.core.ApplicationListener;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Y.B.H(mium2) on 16. 6. 14..
 */
public class RedisAllocateUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static final RedisAllocateUserService instance = new RedisAllocateUserService();
    public final static String FAIL_INDEX_APPEND = "FAIL_";
    public final static String REDIS_BROKERUSER_TABLE = "BROKER_USERCNT";
    public final static String REDIS_FAILBROKERID_TABLE = "FAIL_BROKERID";
    private RedisTemplate<String,Object> masterRedisTemplate; //!!!주의!!! 쓰기/삭제 전용
    private RedisTemplate<String,Object> slaveRedisTemplate;  //!!!주의!!! 읽기 전용
    private SqlSessionTemplate sqlSession;
    // BROKER 서버 정보
    private Map<String,ServerInfoBean> SERVERINFO_MAP = new ConcurrentHashMap<String, ServerInfoBean>();

    public static RedisAllocateUserService getInstance(){
        return instance;
    }

    private RedisAllocateUserService(){
        masterRedisTemplate = (RedisTemplate<String,Object>)ApplicationListener.wContext.getBean("masterRedisTemplate");
        slaveRedisTemplate = (RedisTemplate<String,Object>)ApplicationListener.wContext.getBean("slaveRedisTemplate");
        sqlSession = (SqlSessionTemplate)ApplicationListener.wContext.getBean("sqlSession");
        init();
    }

    private void init(){
        // RDB로 부터 서버정보 가져오고 REDIS로 부너 UPNS 할당된 유저수 정보를 만든다.
        try {
            // UPMC 구동시 서버정보를 로드한다.
            List<ServerInfoBean> db_upnsInfos = sqlSession.selectList("query.getServerInfo");
            if (db_upnsInfos == null || db_upnsInfos.size() == 0) {
                logger.error("##[RedisAllocateUserService] Did not registe messenger servers. You must be registered to broker Servers in RDB!]");
                Runtime.getRuntime().halt(0);
            }


            for (ServerInfoBean serverInfoBean : db_upnsInfos) {
                SERVERINFO_MAP.put(serverInfoBean.getSERVERID(), serverInfoBean);
                // REDIS로 부터 서버별 유저카운트가 등록되어있는지 확인 후 없으면 새로 만들어 넣는다.
                if (!masterRedisTemplate.opsForHash().hasKey(REDIS_BROKERUSER_TABLE, serverInfoBean.getSERVERID())) {
                    // 서비스 중지로 변경되어있는지도 확인한 후 없으면 새로 등록한다.
                    if(!masterRedisTemplate.opsForHash().hasKey(REDIS_BROKERUSER_TABLE, FAIL_INDEX_APPEND+serverInfoBean.getSERVERID())) {
                        masterRedisTemplate.opsForHash().put(REDIS_BROKERUSER_TABLE, serverInfoBean.getSERVERID(), "0");
                    }
                }

            }

        } catch (Exception e) {
            System.out.println("##### RDB에서 서버 정보 로드하는 과정에서 에러 발생~!");
            e.printStackTrace();
            Runtime.getRuntime().halt(0);
        }
    }

    /**
     * 서비스 가입시 할당된 UPNS정보를 보내준다.
     * @return
     */
    public synchronized BrokerConInfoBean allocateBrokerConInfo(){
        return getMinUserAllocate();
    }

    private BrokerConInfoBean getMinUserAllocate() {
        logger.trace("#### [MIN USER] Allocate Type Call!!");
        // REDIS에 저장되어 있는 유저가 가장 적게 할당되어 있는 UPNS 서버그룹아이디 또는 서버아이디를 찾아 클라언트가 붙을 아이피/포트를 구한다.
        BrokerConInfoBean brokerConInfoBean = null;;

        String allocateServerID = "";
        int minAllocateValue =  0;
        int loopCnt = 0;

        Map<Object,Object> redis_upnsUserCntMap = slaveRedisTemplate.opsForHash().entries(REDIS_BROKERUSER_TABLE);
        Set<Map.Entry<Object,Object>> redis_upnsUserCntMapSet = redis_upnsUserCntMap.entrySet();
        for(Map.Entry<Object,Object> mapEntry: redis_upnsUserCntMapSet){
            String key = mapEntry.getKey().toString();
            if(!key.startsWith(FAIL_INDEX_APPEND)) { //장애중인 브로커서버에는 할당하지 않도록 하기 위해.
                int value = 0;
                Object obj = mapEntry.getValue();
                if (obj != null) {
                    value = Integer.parseInt(obj.toString());
                }
                if (loopCnt == 0) {
                    allocateServerID = key;
                    minAllocateValue = value;
                }
                if (minAllocateValue > value) {
                    allocateServerID = key;
                    minAllocateValue = value;
                }
                loopCnt++;
            }

        }
        logger.trace("##### 붙을 서버 정보 : " + allocateServerID);
        if(!allocateServerID.equals("")) {
            brokerConInfoBean = new BrokerConInfoBean();
            ServerInfoBean serverInfoBean = SERVERINFO_MAP.get(allocateServerID);
            brokerConInfoBean.setIP(serverInfoBean.getIP());
            brokerConInfoBean.setPORT(serverInfoBean.getPORT());
            brokerConInfoBean.setSERVERID(allocateServerID);
            brokerConInfoBean.setIsGROUP(false);
        }
        return brokerConInfoBean;
    }

    // 할당된 브로커 서버에 유저수를 증가시킨다.
    public void plusUpnsUserCnt(String allocateServerID){
        masterRedisTemplate.opsForHash().increment(REDIS_BROKERUSER_TABLE, allocateServerID, 1);
    }

    // 할당된 브로커 서버에 유저수를 감소시킨다.
    public void minusUpnsUserCnt(String allocateServerID){
        masterRedisTemplate.opsForHash().increment(REDIS_BROKERUSER_TABLE, allocateServerID, -1);
    }
    // 할당된 브로커서버가 변경되었을 때 호출
    public void changeBrokerUserCnt(String upCntServerID, String downCntServerID){
        masterRedisTemplate.opsForHash().increment(REDIS_BROKERUSER_TABLE,upCntServerID,1);
        // 서비스 중지된 서버아이디의 유저카운트 수를 하나 감소시킨다.
        masterRedisTemplate.opsForHash().increment(REDIS_BROKERUSER_TABLE,FAIL_INDEX_APPEND+downCntServerID,-1);
    }

    /**
     * 브로커서버별 할당되어 있는 유저수를 리턴한다.
     * @return
     */
    public Map<Object,Object> getUpnsAllocateInfo(){
        return masterRedisTemplate.opsForHash().entries(REDIS_BROKERUSER_TABLE);
    }

    /**
     * 클라이언트가 붙을 브로커정보.
     * @param req_serverID
     * @return
     * @throws Exception
     */
    public BrokerConInfoBean getClientConnectUpnsInfo(String req_serverID) throws Exception{
        BrokerConInfoBean brokerConInfoBean = null;
        if(SERVERINFO_MAP.containsKey(req_serverID)) {
            ServerInfoBean serverInfoBean = SERVERINFO_MAP.get(req_serverID);
            brokerConInfoBean = new BrokerConInfoBean();
            brokerConInfoBean.setIP(serverInfoBean.getIP());
            brokerConInfoBean.setPORT(serverInfoBean.getPORT());
            brokerConInfoBean.setSERVERID(req_serverID);
            brokerConInfoBean.setIsGROUP(false);
        }
        return brokerConInfoBean;
    }

    public void regServiceOutBroker(String serverID){
        //STEP 1. BROKER_USER 키테이블에 해당 서버아이디를 앞에 FAIL_이라는 인덱스를 붙여 해당 서버에 할당을 못하도록 한다.
        //STEP 2. 장애서버로 등록하여 유저 로그인시 접속서버를 변경시킨다.
        Object userCntObj = slaveRedisTemplate.opsForHash().get(REDIS_BROKERUSER_TABLE,serverID);
        if(userCntObj!=null){
            // 서버아이디에 "FAIL_" 인덱스를 붙여 클라이언트에 해당 브로커를 할당하지 않도록 처리
            masterRedisTemplate.opsForHash().put(REDIS_BROKERUSER_TABLE,FAIL_INDEX_APPEND+serverID,userCntObj.toString());
            masterRedisTemplate.opsForHash().delete(REDIS_BROKERUSER_TABLE,serverID);
        }else{
            masterRedisTemplate.opsForHash().put(REDIS_BROKERUSER_TABLE, serverID, "0");
        }
        masterRedisTemplate.opsForHash().put(REDIS_FAILBROKERID_TABLE, serverID, userCntObj.toString());
    }

    public void regServiceOnBroker(String serverID){
        Object userCntObj = slaveRedisTemplate.opsForHash().get(REDIS_BROKERUSER_TABLE,FAIL_INDEX_APPEND+serverID);
        if(userCntObj!=null){
            // 서버아이디에 "FAIL_" 인덱스를 붙여 클라이언트에 해당 브로커를 할당하지 않도록 처리
            masterRedisTemplate.opsForHash().put(REDIS_BROKERUSER_TABLE,serverID,userCntObj.toString());
        }else{
            masterRedisTemplate.opsForHash().put(REDIS_BROKERUSER_TABLE,serverID,"0");
        }
        masterRedisTemplate.opsForHash().delete(REDIS_FAILBROKERID_TABLE, serverID);
    }

    public boolean statusBrokerService(String serverID){
        Boolean isOffServer = slaveRedisTemplate.opsForHash().hasKey(REDIS_FAILBROKERID_TABLE,serverID);
        if(isOffServer){
            return false;
        } else {
            return true;
        }
    }

    public Map<String, ServerInfoBean> getSERVERINFO_MAP() {
        return SERVERINFO_MAP;
    }
}
