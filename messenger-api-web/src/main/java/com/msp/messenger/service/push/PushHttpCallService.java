package com.msp.messenger.service.push;

import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.util.HttpClientUtil;
import com.msp.messenger.util.JsonObjectConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 6. 21..
 */
@Service
public class PushHttpCallService {
    private final String PUSH_REG_PUSHSERVICENUSER_URI = "new_rcv_register_service_and_user.ctl";
    private final String PUSH_SENDMSG_URI = "rcv_register_message.ctl";
    private final String PUSH_UPREGISTUSER = "rcv_delete_service.ctl";

    @Value("${upmc.hostname:http://loalhost:8080/}")
    private String UPMC_HOSTNAME;
    @Value("${upmc.servicecode:PUBLIC}")
    private String SERVICECODE;
    @Value("${upmc.sendercode:MESSENGER-API-WEB}")
    private String SENDERCODE;
    @Value("${upmc.sendType:S}")
    private String SENDTYPE;

    public Map<String,Object> callRegPushServiceAndUser(UserInfoBean req_userInfoBean, String APNS_MODE) throws Exception{
        HttpClientUtil httpClientUtil = new HttpClientUtil(null);
        Map<String, String> httpHeadParam = new HashMap<String, String>();
        httpHeadParam.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, Object> postParam = new HashMap<String, Object>();
        String DEVICE_TYPE = "A";
        if(req_userInfoBean.getPUSH_SERVER().equals("APNS")){
            DEVICE_TYPE = "I";
        }

        postParam.put("APP_ID",req_userInfoBean.getAPPID());
        postParam.put("PNSID",req_userInfoBean.getPUSH_SERVER());
        postParam.put("DEVICE_ID",req_userInfoBean.getDEVICEID());
        postParam.put("DEVICE_TYPE",DEVICE_TYPE);
        postParam.put("CUID",req_userInfoBean.getUSERID());
        postParam.put("PSID",req_userInfoBean.getPUSH_TOKEN());
        postParam.put("CNAME",req_userInfoBean.getNICKNAME());
        postParam.put("CBSID",req_userInfoBean.getAPPID());
        postParam.put("APNS_MODE",APNS_MODE);

        httpClientUtil.httpPostConnect(UPMC_HOSTNAME + PUSH_REG_PUSHSERVICENUSER_URI, httpHeadParam, postParam, HttpClientUtil.requestConfig);
        return httpClientUtil.sendForJsonResponse(null);
    }

    public Map<String,Object> pushSend(Set<String> userIdS,String alertMsg,String appid) throws Exception{
        HttpClientUtil httpClientUtil = new HttpClientUtil(null);
        Map<String, String> httpHeadParam = new HashMap<String, String>();
        httpHeadParam.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("APP_ID",appid);
        postParam.put("MESSAGE", alertMsg);
        postParam.put("SERVICECODE",SERVICECODE);
        postParam.put("TYPE",SENDTYPE);
        postParam.put("CUID",JsonObjectConverter.getAsJSON(userIdS));
        postParam.put("SENDERCODE",SENDERCODE);

        httpClientUtil.httpPostConnect(UPMC_HOSTNAME + PUSH_SENDMSG_URI, httpHeadParam, postParam, HttpClientUtil.requestConfig);
        return httpClientUtil.sendForJsonResponse(null);
    }

    public Map<String,Object> unRegistPush(String appid, String deviceID, String pushServer, String userid, String pushToken) throws Exception{
        HttpClientUtil httpClientUtil = new HttpClientUtil(null);
        Map<String, String> httpHeadParam = new HashMap<String, String>();
        httpHeadParam.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, Object> postParam = new HashMap<String, Object>();
        postParam.put("APP_ID",appid);
        postParam.put("PNSID",pushServer);
        postParam.put("DEVICE_ID",deviceID);
        postParam.put("CUID",userid);
        postParam.put("PSID",pushToken);
        httpClientUtil.httpPostConnect(UPMC_HOSTNAME + PUSH_UPREGISTUSER, httpHeadParam, postParam, HttpClientUtil.requestConfig);
        return httpClientUtil.sendForJsonResponse(null);
    }
}
