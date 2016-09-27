package com.msp.messenger.service.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Created by Y.B.H(mium2) on 2016. 9. 20..
 */
@Service
public class RedisMessageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    public final static String REDIS_ROOMID_MSG = "L_ROOMID_";

    @Autowired
    @Qualifier("masterRedisTemplate")
    private RedisTemplate<String,Object> masterRedisTemplate;//!!!주의!!! 쓰기/삭제 전용

    @Autowired
    @Qualifier("slaveRedisTemplate")
    private RedisTemplate<String,Object> slaveRedisTemplate;//!!!주의!!! 읽기 전용


    public List<Object> getMessageHistory(String roomID,int getMessageSize){
        long totalSize = slaveRedisTemplate.opsForList().size(REDIS_ROOMID_MSG + roomID);
        long startPos = 0;
        if(totalSize>getMessageSize){
            startPos = totalSize-getMessageSize;
        }
        List<Object> objMsgList = slaveRedisTemplate.opsForList().range(REDIS_ROOMID_MSG + roomID, startPos, totalSize);
        return objMsgList;
    }
}
