package com.msp.chat.server.storage;

import com.msp.chat.server.bean.ServerInfoBean;
import com.msp.chat.server.service.HttpApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 6. 24..
 */
public class BrokerConInfoStore {
    private Logger logger = LoggerFactory.getLogger("server");

    private Map<String,ServerInfoBean> serverInfBeanMap = new HashMap<String, ServerInfoBean>();
    private static BrokerConInfoStore instance = null;

    private BrokerConInfoStore(){}

    public void init() throws Exception{
        Map<String, Object> responseMap = (Map<String, Object>) HttpApiService.getInstance().getBrokerServerInfo();

        Map<String, String> headMap = (Map<String, String>) responseMap.get("HEADER");
        Map<String, Object> bodyMap = (Map<String, Object>) responseMap.get("BODY");

        if (headMap.get("RESULT_CODE").equals("200")) {
            Map<String, Object> brokerInfoMap = (Map<String, Object>) bodyMap.get("brokerInfo");
            Set<Map.Entry<String, Object>> brokerServerInfoSet = brokerInfoMap.entrySet();
            for (Map.Entry<String, Object> mapEntry : brokerServerInfoSet) {
                String BROKERID = mapEntry.getKey();
                Map<String,String> serverInfoMap = (Map<String,String>) mapEntry.getValue();
                ServerInfoBean serverInfoBean = new ServerInfoBean();
                serverInfoBean.setIP(serverInfoMap.get("ip"));
                serverInfoBean.setPORT(serverInfoMap.get("port"));
                serverInfoBean.setSERVERID(BROKERID);
                serverInfBeanMap.put(BROKERID, serverInfoBean);
                logger.info("######[RedisStorageService init]BROKER SERVER COUNT : "+ serverInfBeanMap.size());
            }
            if(serverInfBeanMap.size()==0){
                throw new Exception("RDB에 브로커 서버 정보를 먼저 셋팅해 주세요.");
            }
        } else {
            throw new Exception("브로커 서버 정보를 가져오는데 실패하였습니다.");
        }

    }
    public static BrokerConInfoStore getInstance(){
        if(instance==null){
            instance = new BrokerConInfoStore();
        }
        return  instance;
    }

    public void putServerInfoBean(String serverID, ServerInfoBean serverInfoBean){
        serverInfBeanMap.put(serverID,serverInfoBean);
    }

    public Map<String, ServerInfoBean> getServerInfBeanMap() {
        return serverInfBeanMap;
    }

    public ServerInfoBean getServerInfoBean(String brokerID){
        return serverInfBeanMap.get(brokerID);
    }
}
