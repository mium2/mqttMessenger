package com.msp.chat.server.worker;

import com.msp.chat.server.Server;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.ehcache.CacheOfflineMsgStore;
import com.msp.chat.server.storage.ehcache.CachePublishStore;
import com.msp.chat.server.storage.ehcache.CacheQos2Store;
import com.msp.chat.server.storage.redis.RedisStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckCacheExpireWorker implements Runnable{
	private static final Logger LOGGER = LoggerFactory.getLogger("server");
	private final RedisStorageService redisStorageService;

	public CheckCacheExpireWorker(){
		redisStorageService = (RedisStorageService)Server.ctx.getBean("redisStorageService");
	}
	
	@Override
	public void run() {
        int term = BrokerConfig.getIntProperty(BrokerConfig.OFFMSG_CHECK_INTERVAL);
        int loop = term * 60;
//		int loop = 60;
        LOGGER.info("###[CheckCacheExpireWorker run] !!!!!!! offmsg check loop Time: "+loop+" sec");
		while(true){
			try{
                for(int i=0; i<loop; i++) {
                    Thread.sleep(1000);
                    CachePublishStore.getInstance().evictExpiredElements();
//                    CacheQos2Store.getInstance().evictExpiredElements();
                }
				// Redis 오프메세지 검사하여 삭제(실패)처리한다.
				redisStorageService.offMsgExpireCheck();

			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
