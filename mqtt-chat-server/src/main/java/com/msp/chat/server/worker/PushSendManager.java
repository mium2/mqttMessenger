package com.msp.chat.server.worker;

import com.msp.chat.server.Server;
import com.msp.chat.server.service.HttpApiService;
import com.msp.chat.server.storage.redis.RedisSubscribeStore;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 7. 20..
 * 브로커 서버가 채팅방 수신자에게 메세지 전달.
 */
public class PushSendManager {
    private final Logger logger = LoggerFactory.getLogger("server");
    public static final RequestConfig requestConfig = RequestConfig.custom()
        .setSocketTimeout(60000)
        .setConnectTimeout(5000)
        .setConnectionRequestTimeout(10000)
        .build();

    private static PushSendManager instance = null;
    private static int MAX_REQUEST = 10000; //한꺼번에 1만껀 까지 발송정보 담을 수있음.
    private PushSendWork[] pushSendWorks;
    private int tail; //다음에 put contents 하는 장소
    private int head; //다음에 take contents 하는 장소
    private int count; //contents 수
    private boolean isShutDown = false;
    private RedisSubscribeStore redisSubscribeStore;

    private final PushSendWorkThread[] pushSendWorkThreads;

    public static PushSendManager getInstance(){
        if(instance==null){
            instance = new PushSendManager(3);
        }
        return instance;
    }

    private PushSendManager(int threadsCnt){
        redisSubscribeStore = (RedisSubscribeStore)Server.ctx.getBean("redisSubscribeStore");
        this.pushSendWorks = new PushSendWork[MAX_REQUEST];
        this.head = 0;
        this.tail = 0;
        this.count = 0;

        // 푸쉬발송 처리 Work 쓰레드인스턴스 올림
        pushSendWorkThreads = new PushSendWorkThread[threadsCnt];
        for(int i=0; i<pushSendWorkThreads.length; i++){
            pushSendWorkThreads[i] = new PushSendWorkThread("pushSendWorkThread-"+i, this);
        }
    }

    private void init(){

    }

    public void startWorkers(){
        init();
        // 푸쉬발송 처리 Work 쓰레드 구동
        for(int i=0; i<pushSendWorkThreads.length; i++){
            pushSendWorkThreads[i].start();
        }
    }

    public synchronized void putWork(PushSendWork _works){
        while (count>0) {  //현재 큐에 있는 메세지를 발송 중이면 대기
            try {
                logger.trace("###[PushSendManager putWork] PushSendWorkThread WAIT! WORK THREAD NAME : (" + Thread.currentThread().getName() + ")  WAITING!");
                wait();
            } catch (InterruptedException e) {
                logger.error("###[PushSendManager putWork] PushSendWorkThread wait() executting Error");
                e.printStackTrace();
            }
        }
        pushSendWorks[tail] = _works;
        tail = (tail + 1) % pushSendWorks.length;
        count++;
        notifyAll();
    }

    public synchronized PushSendWork takeWork() throws InterruptedException {
        while(count <= 0){ //큐에 발송 할 메세지가 없을 경우
            try{
                logger.trace("###[PushSendManager takeWork] PushSendWorkThread WAIT WORK THREAD NAME : (" + Thread.currentThread().getName() + ")  WAITING!");
                wait();
            }catch(InterruptedException e){
                logger.info("###[PushSendManager takeWork] PushSendWorkThread : Shut Down");
                isShutDown = true;
                throw new InterruptedException("ShutDown Call~!");
            }
        }

        PushSendWork xWork = pushSendWorks[head];
        pushSendWorks[head] = null;
        head = (head + 1) % pushSendWorks.length;
        count--;

        if(!isShutDown) {
            notifyAll();
        }
        return xWork;
    }

    public RedisSubscribeStore getRedisSubscribeStore() {
        return redisSubscribeStore;
    }

    public void destory(){
        try {
            if (pushSendWorkThreads != null) {
                for (int i = 0; i < pushSendWorkThreads.length; i++) {
                    if(pushSendWorkThreads[i]!=null){
                        pushSendWorkThreads[i].setRun(false);
                        pushSendWorkThreads[i].interrupt();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
