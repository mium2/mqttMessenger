package com.msp.chat.server.worker;

import com.msp.chat.server.Server;
import com.msp.chat.server.bean.WebSocketMsgBean;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.ehcache.CacheOfflineMsgStore;
import com.msp.chat.server.storage.ehcache.CachePublishStore;
import com.msp.chat.server.storage.ehcache.CacheQos2Store;
import com.msp.chat.server.storage.redis.RedisStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 7. 19..
 */
public class WebSocketMsgManager {
    private final Logger logger = LoggerFactory.getLogger("server");

    private static WebSocketMsgManager instance = null;
    private final RedisStorageService redisStorageService;

    private static int MAX_MESSAGE_QUEUE_SIZE = 1000000; //한꺼번에 100만껀 까지 발송정보 담을 수있음.
    private WebSocketMsgBean[] webSocketMsgBeans;
    private WebSocketWorkerThread[] webSocketWorkerThreads;
    private int tail; //다음에 put contents 하는 장소
    private int head; //다음에 take contents 하는 장소
    private int count; //contents 수
    private static final int WORKER_THREAD_CNT = 3;
    private boolean READYCOMPLETE = false;
    private boolean isShutDown = false;
    private String OFFMSG_STORE_KIND = "1";  // 0:ehcache, 1: redis
    private final String SERVER_ID;
    private final String APPID;


    public static WebSocketMsgManager getInstance(){
        if(instance==null){
            instance = new WebSocketMsgManager(WORKER_THREAD_CNT);
        }
        return instance;
    }

    private WebSocketMsgManager(int threadsCnt){
        SERVER_ID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
        APPID = BrokerConfig.getProperty(BrokerConfig.APPID);
        redisStorageService = (RedisStorageService) Server.ctx.getBean("redisStorageService");
        this.webSocketMsgBeans = new WebSocketMsgBean[MAX_MESSAGE_QUEUE_SIZE];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        webSocketWorkerThreads = new WebSocketWorkerThread[threadsCnt];
    }

    public void startWorkers() throws Exception{
        init();
        OFFMSG_STORE_KIND = BrokerConfig.getProperty(BrokerConfig.OFFMSG_STORE_KIND);
        IAuthenticator authenticator = new FileAuthenticator(BrokerConfig.getProperty(BrokerConfig.PASSWORD_FILE));
        for(int i=0; i<webSocketWorkerThreads.length; i++){
            webSocketWorkerThreads[i] = new WebSocketWorkerThread("WebSocketWorkerThread-"+i, this,authenticator);
            webSocketWorkerThreads[i].start();
        }
    }

    private void init() throws Exception{
    }

    public synchronized void putWebSocketMsgBean(WebSocketMsgBean webSocketMsgBean){
        while (count >= webSocketMsgBeans.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.error("###[WebSocketMsgManager putWebSocketMsgBean]: putWork MaxWork reached! Interrupted Call. get up~~!");
//                e.printStackTrace();
            }
        }

        webSocketMsgBeans[tail] = webSocketMsgBean;
        tail = (tail + 1) % webSocketMsgBeans.length;
        count++;
        notifyAll();
    }

    public synchronized WebSocketMsgBean takeWebSocketMsgBean(){
        while(count <= 0){
            try{
                if(logger.isTraceEnabled()) {
                    logger.trace("###[WebSocketMsgManager takeWebSocketMsgBean] WORK THREAD NAME : {} WAITING!" , Thread.currentThread().getName());
                }
                wait();
            }catch(InterruptedException e){
                logger.info("###[WebSocketMsgManager takeWebSocketMsgBean] Send Thread: Shut Down");
                isShutDown = true;
            }
        }

        WebSocketMsgBean webSocketMsgBean = webSocketMsgBeans[head];
        webSocketMsgBeans[head] = null;
        head = (head + 1) % webSocketMsgBeans.length;
        count--;
        if(!isShutDown) {
            notifyAll();
        }
        return webSocketMsgBean;
    }

    public RedisStorageService getRedisStorageService() {
        return redisStorageService;
    }

    public String getSERVER_ID() {
        return SERVER_ID;
    }

    public String getAPPID() {
        return APPID;
    }

    public void processStop() {
        CacheQos2Store.getInstance().shutdown();
        CachePublishStore.getInstance().shutdown();
        CacheOfflineMsgStore.getInstance().shutdown();
        for(int i=0; i<webSocketWorkerThreads.length; i++){
            if(webSocketWorkerThreads[i]!=null && webSocketWorkerThreads[i].isAlive()){
                webSocketWorkerThreads[i].setRun(false);
            }
        }
    }

}
