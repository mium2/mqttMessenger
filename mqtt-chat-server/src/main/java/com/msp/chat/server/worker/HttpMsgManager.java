package com.msp.chat.server.worker;

import com.msp.chat.server.Server;
import com.msp.chat.server.bean.HttpRequestBean;
import com.msp.chat.server.storage.redis.RedisStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 8. 3..
 */
public class HttpMsgManager {
    private final Logger logger = LoggerFactory.getLogger("server");

    private static HttpMsgManager instance = null;
    private final RedisStorageService redisStorageService;

    private static int MAX_MESSAGE_QUEUE_SIZE = 3000; //한꺼번에 3천껀 까지 Request정보 담을 수있음.
    private HttpRequestBean[] httpRequestBeans;
    private HttpMsgWorkerThread[] httpMsgWorkerThreads;
    private int tail; //다음에 put contents 하는 장소
    private int head; //다음에 take contents 하는 장소
    private int count; //contents 수
    private static final int WORKER_THREAD_CNT = 3;
    private boolean isShutDown = false;
    private Set<String> CHKIMGSET;

    public static HttpMsgManager getInstance(){
        if(instance==null){
            instance = new HttpMsgManager(WORKER_THREAD_CNT);
        }
        return instance;
    }

    private HttpMsgManager(int threadsCnt){
        redisStorageService = (RedisStorageService) Server.ctx.getBean("redisStorageService");
        this.httpRequestBeans = new HttpRequestBean[MAX_MESSAGE_QUEUE_SIZE];
        this.head = 0;
        this.tail = 0;
        this.count = 0;
        httpMsgWorkerThreads = new HttpMsgWorkerThread[threadsCnt];
        CHKIMGSET = new HashSet<String>();
        CHKIMGSET.add("jpg");
        CHKIMGSET.add("jpeg");
        CHKIMGSET.add("gif");
        CHKIMGSET.add("bmp");
        CHKIMGSET.add("png");
        CHKIMGSET.add("tif");
    }

    public void startWorkers() throws Exception{
        init();
        for(int i=0; i<httpMsgWorkerThreads.length; i++){
            httpMsgWorkerThreads[i] = new HttpMsgWorkerThread("HttpMsgWorkerThread-"+i,this);
            httpMsgWorkerThreads[i].start();
        }
    }

    private void init() throws Exception{
    }

    public synchronized void putRequestBean(HttpRequestBean httpRequestBean){
        while (count >= httpRequestBeans.length) {
            try {
                wait();
            } catch (InterruptedException e) {
                logger.error("###[HttpMsgManager putRequest]: putWork MaxWork reached! Interrupted Call. get up~~!");
//                e.printStackTrace();
            }
        }

        httpRequestBeans[tail] = httpRequestBean;
        tail = (tail + 1) % httpRequestBeans.length;
        count++;
        notifyAll();
    }

    public synchronized HttpRequestBean takeRequestBean(){
        while(count <= 0){
            try{
                if(logger.isTraceEnabled()) {
                    logger.trace("###[HttpMsgManager takeRequestBean] WORK THREAD NAME : {} WAITING!" , Thread.currentThread().getName());
                }
                wait();
            }catch(InterruptedException e){
                logger.info("###[HttpMsgManager takeRequestBean] Send Thread: Shut Down");
                isShutDown = true;
            }
        }

        HttpRequestBean httpRequestBean = httpRequestBeans[head];
        httpRequestBeans[head] = null;
        head = (head + 1) % httpRequestBeans.length;
        count--;
        if(!isShutDown) {
            notifyAll();
        }
        return httpRequestBean;
    }

    public Set<String> getCHKIMGSET() {
        return CHKIMGSET;
    }

    public void setCHKIMGSET(Set<String> CHKIMGSET) {
        this.CHKIMGSET = CHKIMGSET;
    }

    public void processStop() {
        for(int i=0; i<httpMsgWorkerThreads.length; i++){
            if(httpMsgWorkerThreads[i]!=null && httpMsgWorkerThreads[i].isAlive()){
                httpMsgWorkerThreads[i].setRun(false);
            }
        }
    }
}
