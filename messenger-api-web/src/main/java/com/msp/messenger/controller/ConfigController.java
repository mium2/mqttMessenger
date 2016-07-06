package com.msp.messenger.controller;

import com.msp.messenger.bean.ServerInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisAllocateUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.JsonObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 24..
 */
@Controller
public class ConfigController {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    RedisUserService redisUserService;

    @Autowired(required = true)
    RdbUserService rdbUserService;

    // 메신저서버 서비스 OFF 브로커 아이디를 등록한다.
    @RequestMapping(value = "/regServiceOffServer.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String regServiceOffServer(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_BROKERID = request.getParameter("BROKERID");

        if(req_BROKERID == null || req_BROKERID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                RedisAllocateUserService.getInstance().regServiceOutBroker(req_BROKERID);
            } else {
                return responseJson(resultHeadMap, resultBodyMap);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }
        return responseJson(resultHeadMap,resultBodyMap);
    }

    // 메신저서버 서비스 ON 브로커서버 아이디를 등록한다.
    @RequestMapping(value = "/regServiceOnServer.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String regServiceOnServer(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        String req_BROKERID = request.getParameter("BROKERID");

        if(req_BROKERID == null || req_BROKERID.equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap);
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                RedisAllocateUserService.getInstance().regServiceOnBroker(req_BROKERID);
            } else {
                return responseJson(resultHeadMap, resultBodyMap);
            }
        }catch (Exception e){
            e.printStackTrace();
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }
        return responseJson(resultHeadMap,resultBodyMap);
    }

    @RequestMapping(value = "/getBrokerServerInfo.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String getBrokerServerInfo(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                Map<String, ServerInfoBean> brokerInfoMap = RedisAllocateUserService.getInstance().getSERVERINFO_MAP();
                resultBodyMap.put("brokerInfo",brokerInfoMap);
            } else {
                return responseJson(resultHeadMap, resultBodyMap);
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }
        return responseJson(resultHeadMap,resultBodyMap);
    }

    private String responseJson(Map<String,String> resultHeadMap,Map<String,Object> resultBodyMap){

        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[ConfigController] :" + responseJson);
        return responseJson;
    }
}
