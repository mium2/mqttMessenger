package com.msp.chat.server.worker;

import com.msp.chat.server.service.HttpApiService;
import com.msp.chat.server.storage.redis.RedisSubscribeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 6. 29..
 */
public class PushSendWork implements Serializable{
    private static final long serialVersionUID = 2086333882495305577L;
    Logger logger = LoggerFactory.getLogger("server");

    private String APP_ID = "";
    private String MESSAGE = "";
    private String CUID = "";

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setAPP_ID(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public void setMESSAGE(String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }

    public void setCUID(String CUID) {
        this.CUID = CUID;
    }

    public void workExecute(RedisSubscribeStore redisSubscribeStore) throws Exception{      ///각 push 메세지 전송 구현
        try {
            Map<String,Object> responseMap = HttpApiService.getInstance().pushSend(CUID, MESSAGE, APP_ID);
            Map<String,String> headResponseMap = (Map<String,String>)responseMap.get("HEADER");
            if(!headResponseMap.get("RESULT_CODE").equals("0000")){
                logger.info("###[PushSendWork workExecute] PUSH SEND ERR RESULT :"+headResponseMap.get("RESULT_BODY"));
            }
            if(logger.isDebugEnabled()){
                logger.debug("###[PushSendWork workExecute] push message send Success~~!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
