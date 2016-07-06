package com.msp.chat.server.worker;

import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.server.Server;
import com.msp.chat.server.bean.events.LostConnectionEvent;
import com.msp.chat.server.bean.events.PingEvent;
import com.msp.chat.server.bean.events.ProtocolEvent;
import com.msp.chat.server.bean.events.ValueEvent;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.netty.ServerChannel;
import com.msp.chat.server.storage.BrokerConInfoStore;
import com.msp.chat.server.storage.ehcache.CacheOfflineMsgStore;
import com.msp.chat.server.storage.ehcache.CachePublishStore;
import com.msp.chat.server.storage.ehcache.CacheQos2Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Y.B.H(mium2) on 16. 4. 11..
 */
public class MqttMsgWorkerManager {
    private final Logger logger = LoggerFactory.getLogger("server");

    private static MqttMsgWorkerManager instance = null;

    private static int MAX_MESSAGE_QUEUE_SIZE = 1000000; //한꺼번에 100만껀 까지 발송정보 담을 수있음.
    private ValueEvent[] valueEventQueue;
    private MqttMsgWorkerThread[] mqttMsgWorkerThreadPool;
    private int tail; //다음에 put contents 하는 장소
    private int head; //다음에 take contents 하는 장소
    private int count; //contents 수
    private static final int WORKER_THREAD_CNT = 5;
    private boolean READYCOMPLETE = false;
    private boolean isShutDown = false;
    private String OFFMSG_STORE_KIND = "1";  // 0:ehcache, 1: redis


    public static MqttMsgWorkerManager getInstance(){
        if(instance==null){
            instance = new MqttMsgWorkerManager(WORKER_THREAD_CNT);
        }
        return instance;
    }

    private MqttMsgWorkerManager(int threadsCnt){

        this.valueEventQueue = new ValueEvent[MAX_MESSAGE_QUEUE_SIZE];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        mqttMsgWorkerThreadPool = new MqttMsgWorkerThread[threadsCnt];
    }

    public void startWorkers() throws Exception{
        init();
        OFFMSG_STORE_KIND = BrokerConfig.getProperty(BrokerConfig.OFFMSG_STORE_KIND);
        IAuthenticator authenticator = new FileAuthenticator(BrokerConfig.getProperty(BrokerConfig.PASSWORD_FILE));
        for(int i=0; i<mqttMsgWorkerThreadPool.length; i++){
            mqttMsgWorkerThreadPool[i] = new MqttMsgWorkerThread("MqttMsgWorkerThread-"+i, this,authenticator);
            mqttMsgWorkerThreadPool[i].start();
        }
    }

    private void init() throws Exception{
        ////////////////////////////////////////////////////////////////////////////
        // WEB-API 서버로 부터 브로커 서버 정보 가져옴
        ////////////////////////////////////////////////////////////////////////////
        BrokerConInfoStore.getInstance().init();

        ////////////////////////////////////////////////////////////////////////////
        // Qos2 echche 초기화
        ////////////////////////////////////////////////////////////////////////////
        CacheQos2Store.getInstance().getCacheManager(Server.CACHE_CONF_FILE);
        CacheQos2Store.getInstance().getCache("qos2Store");
        // 현재 Qos2 발송메세지 스토어
        CacheQos2Store.getInstance().getSize();

        ////////////////////////////////////////////////////////////////////////////
        // 보낸 publish 메세지 캐쉬데이타 처리하기 위해 echche 초기화
        ////////////////////////////////////////////////////////////////////////////
        CachePublishStore.getInstance().getCacheManager(Server.CACHE_CONF_FILE);
        CachePublishStore.getInstance().getCache("publishStore");

        ////////////////////////////////////////////////////////////////////////////
        // offmessage storage를 ehcache를 사용한다고 설정시.보내지 못한 Offline 메세지 캐쉬데이타 처리하기 위해 echche 초기화
        ////////////////////////////////////////////////////////////////////////////
        if(OFFMSG_STORE_KIND.equals("0")) {
            CacheOfflineMsgStore.getInstance().getCacheManager(Server.CACHE_CONF_FILE);
            CacheOfflineMsgStore.getInstance().getCache("offlinestore");
            logger.debug("###[MqttMsgWorkerManager init] " + CacheOfflineMsgStore.getInstance().getSize());
        }
    }

    public void handleProtocolMessage(ServerChannel session, AbstractMessage msg) {
        ProtocolEvent protocolEvent = new ProtocolEvent(session, msg);
        ValueEvent event = new ValueEvent();
        event.setEvent(protocolEvent);
        putMqttMsg(event);
    }

    public void receivePing(String clientID){
        ValueEvent event = new ValueEvent();
        event.setEvent(new PingEvent(clientID));
        putMqttMsg(event);

    }

    //idle체크에 의해 네트웍상태가 끊긴 클라이언트가 발생시 호출됨
    public void lostConnection(String clientID) {
        ValueEvent event = new ValueEvent();
        event.setEvent(new LostConnectionEvent(clientID));
        putMqttMsg(event);
    }

    public synchronized void putMqttMsg(ValueEvent _valueEvent){
        while (count >= valueEventQueue.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.error("###[MqttMsgWorkerManager putMqttMsg]: putWork MaxWork reached! Interrupted Call. get up~~!");
//                e.printStackTrace();
            }
        }

        valueEventQueue[tail] = _valueEvent;
        tail = (tail + 1) % valueEventQueue.length;
        count++;
        notifyAll();
    }

    public synchronized ValueEvent takeMqttMsg(){
        while(count <= 0){
            try{
                if(logger.isTraceEnabled()) {
                    logger.trace("###[MqttMsgWorkerManager takeMqttMsg] WORK THREAD NAME : {} WAITING!" , Thread.currentThread().getName());
                }
                wait();
            }catch(InterruptedException e){
                logger.info("###[MqttMsgWorkerManager takeMqttMsg] Send Thread: Shut Down");
                isShutDown = true;
            }
        }

        ValueEvent valueEvent = valueEventQueue[head];
        valueEventQueue[head] = null;
        head = (head + 1) % valueEventQueue.length;
        count--;
        if(!isShutDown) {
            notifyAll();
        }
        return valueEvent;
    }

    public void processStop() {
        CacheQos2Store.getInstance().shutdown();
        CachePublishStore.getInstance().shutdown();
        CacheOfflineMsgStore.getInstance().shutdown();
        for(int i=0; i<mqttMsgWorkerThreadPool.length; i++){
            if(mqttMsgWorkerThreadPool[i]!=null && mqttMsgWorkerThreadPool[i].isAlive()){
                mqttMsgWorkerThreadPool[i].setRun(false);
            }
        }

    }

}
