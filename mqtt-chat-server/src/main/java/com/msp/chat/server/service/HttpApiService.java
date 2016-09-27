package com.msp.chat.server.service;

import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.util.HttpClientUtil;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 7. 18..
 */
public class HttpApiService {
    private static HttpApiService instance = new HttpApiService();

    private final String MESSENGER_WEB_API_HOST;
    private final String UPMC_HOST;
    private final String BROKERSERVER_INFO_URI = "getBrokerServerInfo.ctl";
    private final String PUSH_SENDMSG_URI = "rcv_register_message.ctl";

    private final String PUSH_SERVICECODE;
    private final String PUSH_SENDERCODE;

    public static HttpApiService getInstance(){
        return instance;
    }

    private HttpApiService(){
        MESSENGER_WEB_API_HOST = BrokerConfig.getProperty(BrokerConfig.MESSENGER_WEB_API_SERVER_HOST);
        UPMC_HOST = BrokerConfig.getProperty(BrokerConfig.PUSH_UPMC_HOST);
        PUSH_SERVICECODE = BrokerConfig.getProperty(BrokerConfig.PUSH_SERVICECODE);
        PUSH_SENDERCODE = BrokerConfig.getProperty(BrokerConfig.PUSH_SENDERCODE);
    }

    public Map<String,Object> getBrokerServerInfo() throws Exception{
        HttpClientUtil httpClientUtil = new HttpClientUtil(null);
        Map<String, String> httpHeadParam = new HashMap<String, String>();
        httpHeadParam.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("USERID",BrokerConfig.SERVER_ID);

        try {
            httpClientUtil.httpPostConnect(MESSENGER_WEB_API_HOST + BROKERSERVER_INFO_URI, httpHeadParam, postParam, HttpClientUtil.requestConfig);
        }catch (Exception e){
            e.printStackTrace();
            throw new Exception("##"+MESSENGER_WEB_API_HOST+" 서버 연결 실패 "+e.getMessage());
        }
        return httpClientUtil.sendForJsonResponse(null);
    }

    public Map<String,Object> pushSend(String userId,String alertMsg,String appid) throws Exception{
        HttpClientUtil httpClientUtil = new HttpClientUtil(null);
        Map<String, String> httpHeadParam = new HashMap<String, String>();
        httpHeadParam.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("APP_ID",appid);
        postParam.put("MESSAGE", alertMsg);
        postParam.put("SERVICECODE",PUSH_SERVICECODE);
        postParam.put("TYPE","S");
//        postParam.put("TYPE","E");
        postParam.put("CUID",userId);
        postParam.put("SENDERCODE",PUSH_SENDERCODE);
//        postParam.put("DB_IN","Y");
        System.out.println("#### UPMC_HOST + PUSH_SENDMSG_URI:" + UPMC_HOST + PUSH_SENDMSG_URI);
        httpClientUtil.httpPostConnect(UPMC_HOST + PUSH_SENDMSG_URI, httpHeadParam, postParam, HttpClientUtil.requestConfig);
        return httpClientUtil.sendForJsonResponse(null);
    }
}
