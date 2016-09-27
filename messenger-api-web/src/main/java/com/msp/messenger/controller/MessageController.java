package com.msp.messenger.controller;

import com.msp.messenger.common.Constants;
import com.msp.messenger.service.redis.RedisMessageService;
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
 * Created by Y.B.H(mium2) on 2016. 9. 20..
 */
@Controller
public class MessageController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    RedisMessageService redisMessageService;

    @RequestMapping(value = "/getMessageHistory.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String findFriend(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        if(request.getParameter("ROOMID")==null || request.getParameter("ROOMID").equals("") || request.getParameter("MSG_SIZE")==null || request.getParameter("MSG_SIZE").equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap, resultBodyMap);
        }
        try {
            // ###################### 토큰 인증 결과 처리 시작 #############################
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            //인증에러가 아닐 경우만 비즈니스 로직 수행
            if (!resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) { // 토큰인증체크
                return responseJson(resultHeadMap, resultBodyMap); // 인증 실패 처리
            }
            String userId = resultHeadMap.get("USERID");
            resultHeadMap.remove("USERID"); //토큰에서 추출한 정보를 응답데이터에 넘기지 않기 위해
            // ###################### 토큰 인증결과 처리 마침 ###############################

            int msgSize = Integer.parseInt(request.getParameter("MSG_SIZE").trim());
            List<Object> historyMsg = redisMessageService.getMessageHistory(request.getParameter("ROOMID"),msgSize);
            resultBodyMap.put("historyMsg",historyMsg);

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
        logger.debug("###[MessageController] :" + responseJson);
        return responseJson;
    }
}
