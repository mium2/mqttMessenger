package com.msp.chat.server.storage.redis;

import com.google.gson.Gson;
import com.msp.chat.server.bean.Subscription;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.ISubscribeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 5. 2..
 */
@Service
public class RedisSubscribeStore implements ISubscribeStore {
    private Logger logger = LoggerFactory.getLogger("server");
    private Gson gson = new Gson();

    public String APPID = null;
    public final static String REDIS_SUBSCRIBE = "SUBSCRIBE";
    // 메신저에서 만든 대화방 키테이블
    public final static String REDIS_ROOMID_SUBSCRUBE = ":ROOMID_SUBSCRIBE";

    @Autowired(required = true)
    private RedisTemplate masterRedisTemplate;

    @Autowired(required = true)
    private RedisTemplate slaveRedisTemplate;

    @Override
    public void initPersistentSubscriptions() {

    }

    @Override
    public void addNewSubscription(Subscription newSubscription) {
        HashSet<String> cliendIdSet = new HashSet<String>();
        String clientIdSetJsonString = "";
        String topic = newSubscription.getTopic();
        try {
            Object obj = slaveRedisTemplate.opsForHash().get(REDIS_SUBSCRIBE,topic);
            if(obj!=null){
                clientIdSetJsonString = obj.toString();
                cliendIdSet = gson.fromJson(clientIdSetJsonString, HashSet.class);
            }
            cliendIdSet.add(newSubscription.getClientId());
            clientIdSetJsonString = gson.toJson(cliendIdSet);
            masterRedisTemplate.opsForHash().put(REDIS_SUBSCRIBE,topic,clientIdSetJsonString);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addNewSubscriptionList(List<Subscription> newSubscriptionList) {
        HashSet<String> cliendIdSet = new HashSet<String>();
        String clientIdSetJsonString = "";
        String topic = "";
        try {
            for(int i=0; i<newSubscriptionList.size(); i++){
                Subscription subscription = newSubscriptionList.get(i);
                // 현재 해당 토픽으로 들어있는 cuid HashSet을 구해온다.
                if(i==0){
                    topic = subscription.getTopic();
                    Object obj = slaveRedisTemplate.opsForHash().get(REDIS_SUBSCRIBE, topic);
                    if (obj != null) {
                        clientIdSetJsonString = obj.toString();
                        cliendIdSet = gson.fromJson(clientIdSetJsonString, HashSet.class);
                    }
                }
                //Redis에서 가져온 정보에 신규로 추가되는 구독리스트를 넣는다.
                cliendIdSet.add(subscription.getClientId());
            }
            clientIdSetJsonString = gson.toJson(cliendIdSet);
            masterRedisTemplate.opsForHash().put(REDIS_SUBSCRIBE, topic, clientIdSetJsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getAllSubscribeCount() {
        return slaveRedisTemplate.opsForHash().size(REDIS_SUBSCRIBE);
    }

    @Override
    public HashSet<String> getSubscribeClientID(String topic) throws Exception{
        if(APPID ==null){
            APPID = BrokerConfig.getProperty(BrokerConfig.APPID);
        }
        HashSet<String> cliendIdSet = null;
        // MQTT서버를 통해 Subscribe를 한 경우
//        Object obj = redisTemplate.opsForHash().get(REDIS_SUBSCRIBE, topic);
        // 메신저 API서버를 통해 대화방을 생성한 경우.
        Object obj = slaveRedisTemplate.opsForHash().get(APPID+REDIS_ROOMID_SUBSCRUBE,topic);
        if (obj != null) {
            String clientIdSetJsonString = obj.toString();
            cliendIdSet = gson.fromJson(clientIdSetJsonString, HashSet.class);
        }

        return cliendIdSet;
    }

    @Override
    public void removeSubscription(String topic, String clientID) {
        Object obj = slaveRedisTemplate.opsForHash().get(REDIS_SUBSCRIBE, topic);
        if (obj != null) {
            String clientIdSetJsonString = obj.toString();
            HashSet<String> cliendIdSet = gson.fromJson(clientIdSetJsonString, HashSet.class);
            cliendIdSet.remove(clientID);
            masterRedisTemplate.opsForHash().put(REDIS_SUBSCRIBE, topic, gson.toJson(cliendIdSet));
        }
    }

    @Override
    public List<Subscription> retrieveAllSubscriptions() {
        return null;
    }

    @Override
    public void close() {

    }
}
