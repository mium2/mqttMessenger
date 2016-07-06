package com.msp.chat.server.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 6. 29..
 */
public class PushSendWorkThread extends Thread{
    private Logger log = LoggerFactory.getLogger("server");
    private String ThreadName;
    private final PushSendManager pushSendManager;
    private boolean isRun = true;

    public PushSendWorkThread(String name, PushSendManager _pushSendManager){
        super(name);
        this.ThreadName=getName();
        this.pushSendManager=_pushSendManager;
    }
    public void run(){
        while(isRun){
            ///push 전송 로직
            try {
                PushSendWork pushSendWork = pushSendManager.takeWork();
                pushSendWork.workExecute(pushSendManager.getRedisSubscribeStore());
                pushSendWork = null;
            }catch (InterruptedException ex){
                log.debug("###[PushSendWorkThread run] "+ThreadName+" InterruptedException 발생");
                break;
            }catch (Exception e){
                e.printStackTrace();
                log.debug("###[PushSendWorkThread run] " + ThreadName + " End :" + e.getMessage());
            }
        }
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }
}
