package com.msp.chat.server.storage.ehcache;

import com.msp.chat.server.bean.events.PublishEvent;
import net.sf.ehcache.*;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Created by Y.B.H(mium2) on 15. 8. 25..
 */
public class CacheQos2Store {
    private Logger LOGGER = LoggerFactory.getLogger("server");

    private static CacheManager qos2Cachemanager;
    private static CacheQos2Store instance = null;
    private Cache cache;

    private CacheQos2Store(){}

    public static CacheQos2Store getInstance(){
        if(instance==null){
            instance = new CacheQos2Store();
        }
        return  instance;
    }

    public CacheManager getCacheManager() {
        try {
            URL url = getClass().getResource("ehcache.xml");
            qos2Cachemanager = CacheManager.create(url);
        } catch(CacheException e) {
            e.printStackTrace();
        }
        return qos2Cachemanager;
    }

    public CacheManager getCacheManager(String configFilePath) {
        try {
            qos2Cachemanager = CacheManager.create(configFilePath);
        } catch ( CacheException e) {
            e.printStackTrace();
        }
        return qos2Cachemanager;
    }

    //설정파일에 있는 만료시간기준 지난 것들은 삭제
    public void evictExpiredElements() {
        cache.evictExpiredElements();
        return;
    }

    public Cache getCache(String cacheName) {
        cache = (Cache)qos2Cachemanager.getCache(cacheName);

        cache.getCacheEventNotificationService().registerListener(new CacheEventListener() {
            @Override
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
     * QOS 2 메세지를 캐쉬에 저장한다
     * 동일 topic에 정보가 있다면 LIST에 메시지를 추가한다.
     * @param publishEvent PublishEvent
     * @throws CacheException
     *
     */
    public void put(PublishEvent publishEvent) throws CacheException {
        String publishKey = makePublishKey(publishEvent.getPubClientID(),publishEvent.getMessageID());
        LOGGER.debug("###[CacheQos2Store put] Publish PUT clientID : "+publishEvent.getPubClientID());
        Element element = new Element(publishKey, publishEvent);
        cache.put(element);
        LOGGER.debug("###[CacheQos2Store put]Publish PUT Qos2 Cache size : " + cache.getSize());
    }

    public Element get(String clientID, int messageID) throws CacheException {
        String publishKey = makePublishKey(clientID,messageID);
        return cache.get(publishKey);
    }


    public void remove(String clientID, int messageID) throws Exception{
        remove(clientID + "," + messageID);
    }
    private void remove(String clientID) throws CacheException {
        cache.remove(clientID);
    }

    public int getSize() throws CacheException {
        return cache.getSize();
    }

    private void flush() {
        cache.flush();
    }

    public void shutdown() {
//        flush();
        qos2Cachemanager.shutdown();
    }

    private String makePublishKey(String clientID, int messageID){
        String publishKey = String.format("%s%d", clientID, messageID);
        return publishKey;
    }
}
