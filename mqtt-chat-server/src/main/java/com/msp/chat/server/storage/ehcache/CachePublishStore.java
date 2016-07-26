package com.msp.chat.server.storage.ehcache;

import com.msp.chat.server.Server;
import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.redis.RedisStorageService;
import net.sf.ehcache.*;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by Y.B.H(mium2) on 16. 7. 18..
 */
public class CachePublishStore {
    private Logger LOGGER = LoggerFactory.getLogger("server");

    private static CacheManager publishCachemanager;
    private static CachePublishStore instance = null;
    private Cache cache;
    private final RedisStorageService redisStorageService;
    private final String OFFMSG_STORAGE_KIND; // 0 : ehcache, 1 : reids

    private CachePublishStore(){
        OFFMSG_STORAGE_KIND = BrokerConfig.getProperty(BrokerConfig.OFFMSG_STORE_KIND);
        redisStorageService = (RedisStorageService) Server.ctx.getBean("redisStorageService");
    }

    public static CachePublishStore getInstance(){
        if(instance==null){
            instance = new CachePublishStore();
        }
        return  instance;
    }

    public CacheManager getCacheManager() {
        try {
            URL url = getClass().getResource("ehcache.xml");
            publishCachemanager = CacheManager.create(url);
        } catch(CacheException e) {
            e.printStackTrace();
        }
        return publishCachemanager;
    }

    public CacheManager getCacheManager(String configFilePath) {
        try {
            publishCachemanager = CacheManager.create(configFilePath);
        } catch ( CacheException e) {
            e.printStackTrace();
        }
        return publishCachemanager;
    }

    //설정파일에 있는 만료시간기준 지난 것들은 삭제
    public void evictExpiredElements() {
        try {
            cache.evictExpiredElements();
        }catch (Exception e){}
        return;
    }

    public Cache getCache(String cacheName) {
        cache = (Cache)publishCachemanager.getCache(cacheName);
        cache.getCacheEventNotificationService().registerListener(new CacheEventListener() {
            public Object clone() throws CloneNotSupportedException {
                throw new CloneNotSupportedException();
            }

            public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
            }

            public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {
            }

            public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
            }
            // 지정된 시간안에 삭제 되지 않은 publish메세지는 ack가 안 들어온 것이므로 재 전송처리
            public void notifyElementExpired(Ehcache ehcache, Element element) {
                PublishEvent publishEvent = (PublishEvent)element.getObjectValue();
                if(OFFMSG_STORAGE_KIND.equals("1")) {
                    redisStorageService.putOffMsg(publishEvent.getSubClientID(), publishEvent);
                }else{
                    CacheOfflineMsgStore.getInstance().put(publishEvent.getSubClientID(), publishEvent);
                }
                if(LOGGER.isTraceEnabled()) {
                    LOGGER.trace("###[CachePublishStore getCache] notifyElementExpired 삭제 Publish cache.getSize():" + cache.getSize());
                }
            }

            public void notifyElementEvicted(Ehcache ehcache, Element element) {
            }

            public void notifyRemoveAll(Ehcache ehcache) {
            }

            public void dispose() {
            }
        });
        return cache;
    }

    /**
     * roker=>client로 발송하는 메세지를 임시메시지를 캐쉬에 저장한다.
     * @param subClientID
     * @param messageID
     * @param value
     * @throws CacheException
     */
    public void put(String subClientID, int messageID, PublishEvent value) throws CacheException {
        String publishKey = makePublishKey(subClientID,messageID);
        Element element = new Element(publishKey, value);
        cache.put(element);
    }

    public Element get(String subClientID, int messageID) throws CacheException {
        String publishKey = makePublishKey(subClientID,messageID);
        return cache.get(publishKey);
    }

    public boolean containsKey(String clientID){
        return  cache.isKeyInCache(clientID);
    }
    public void remove(String subClientID, int messageID) throws Exception{

        remove(makePublishKey(subClientID,messageID));
    }
    private void remove(String publishKey) throws CacheException {
        cache.remove(publishKey);
    }

    public int getSize() throws CacheException {
        return cache.getSize();
    }

    private void flush() {
        cache.flush();
    }

    public void shutdown() {
//        flush();
        publishCachemanager.shutdown();
    }

    private String makePublishKey(String subClientID, int messageID){
        String publishKey = String.format("%s%d", subClientID, messageID);
        return publishKey;
    }
}
