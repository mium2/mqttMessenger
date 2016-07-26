package com.msp.chat.server.storage.ehcache;

import com.msp.chat.server.bean.events.PublishEvent;
import net.sf.ehcache.*;
import net.sf.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 7. 18..
 */
public class CacheOfflineMsgStore {
    private Logger LOGGER = LoggerFactory.getLogger("server");
    private static CacheManager OffCacheManager;
    private static CacheOfflineMsgStore instance = null;
    private Cache cache;

    private CacheOfflineMsgStore(){}

    public static CacheOfflineMsgStore getInstance(){
        if(instance==null){
            instance = new CacheOfflineMsgStore();
        }
        return  instance;
    }

    public CacheManager getCacheManager() {
        try {
            URL url = getClass().getResource("ehcache.xml");
            OffCacheManager = CacheManager.create(url);
        } catch(CacheException e) {
            e.printStackTrace();
        }
        return OffCacheManager;
    }

    public CacheManager getCacheManager(String configFilePath) {
        try {
            OffCacheManager = CacheManager.create(configFilePath);
        } catch ( CacheException e) {
            e.printStackTrace();
        }
        return OffCacheManager;
    }
    //설정파일에 있는 만료시간기준 지난 것들은 삭제
    public void evictExpiredElements() {
        cache.evictExpiredElements();
    }

    public Cache getCache(String cacheName) {
        cache = (Cache)OffCacheManager.getCache(cacheName);
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

            public void notifyElementExpired(Ehcache ehcache, Element element) {
                List<PublishEvent> failMsgs = (List<PublishEvent>) element.getObjectValue();
                //TODO : 오프메세지의 저장 보관시간이 끝났을 때 처리
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("###[CacheOfflineMsgStore notifyElementExpired] offmessage ");
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
     * 전송 실패 메시지를 캐쉬에 저장한다
     * 동일 topic에 정보가 있다면 LIST에 메시지를 추가한다.
     * @param clientID connection cleint ID
     * @param publishEvent PublishEvent
     * @throws CacheException
     *
     */
    public void put(String clientID, PublishEvent publishEvent) throws CacheException {
        if(LOGGER.isDebugEnabled()) {
//            LOGGER.debug("##### OFFMSG PUT clientID : "+clientID);
        }
        List<PublishEvent> publishEvents = get(clientID);
        publishEvents.add(publishEvent);
        Element element = new Element(clientID, publishEvents);
        cache.put(element);
    }

    public List<PublishEvent> get(String clientID) throws CacheException {
        List<PublishEvent> storedPublishEventList = null;
        Element element = cache.get(clientID);
        if(element!=null){
            storedPublishEventList = (List<PublishEvent>)element.getObjectValue();
        }else{
            storedPublishEventList = new ArrayList<PublishEvent>();
        }
        return storedPublishEventList;
    }

    public boolean containsKey(String clientID){
        return  cache.isKeyInCache(clientID);
    }

    public void remove(String name) throws CacheException {
        cache.remove(name);
    }

    public String remove(String clientID, int messageID) throws CacheException {
        Element element = cache.get(clientID);
        String pubClientID = null;
        if(element!=null){
            List<PublishEvent> offPublishEvents = (List<PublishEvent>)element.getObjectValue();
            for(int i=0; i<offPublishEvents.size(); i++){
                PublishEvent publishEvent = offPublishEvents.get(i);
                if(publishEvent.getMessageID()==messageID){
                    pubClientID = publishEvent.getPubClientID();
                    offPublishEvents.remove(i);
                }
                break;
            }
            Element editElement = new Element(clientID,offPublishEvents);
            cache.put(editElement);
        }
        return pubClientID;
    }

    public int getSize() throws CacheException {
        return cache.getSize();
    }

    private void flush() {
        cache.flush();
    }

    public void shutdown() {
//        flush();
        OffCacheManager.shutdown();
    }

    public void checkOffCacheExpire() throws Exception {
        List allKeyList = cache.getKeys();

        for (Iterator iter = allKeyList.iterator(); iter.hasNext();){
            Object key = iter.next();
            Element element = cache.getQuiet(key);
            if (element != null) {
                continue;
            }
        }
    }

}
