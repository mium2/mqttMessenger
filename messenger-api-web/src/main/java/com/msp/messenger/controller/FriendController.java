package com.msp.messenger.controller;

import com.msp.messenger.bean.FriendModelBean;
import com.msp.messenger.bean.UserInfoBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.service.rdb.RdbUserService;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.JsonObjectConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 17..
 */
@Controller
public class FriendController {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    RedisUserService redisUserService;

    @Autowired(required = true)
    RdbUserService rdbUserService;

    @RequestMapping(value = "/findFriend.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String findFriend(HttpServletRequest request, HttpServletResponse response, @ModelAttribute FriendModelBean reqFriendModelBean){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();

        if(reqFriendModelBean.getUSERID().equals("") || reqFriendModelBean.getAPPID().equals("") || reqFriendModelBean.getFRIEND_HPLIST().equals("")){
            resultHeadMap.put(Constants.RESULT_CODE_KEY,Constants.ERR_1000);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_1000_MSG);
            return responseJson(resultHeadMap,resultBodyMap, reqFriendModelBean.getAPPID());
        }
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행
                // 친구찾기를 요청한 핸드폰번호로 메신저에 가입한 사용자 정보를 찾아 핸드폰번호와 유저아이디 정보를 넘겨준다.
                Set<Object> hpNumSet = new HashSet<Object>();
                StringTokenizer st = new StringTokenizer(reqFriendModelBean.getFRIEND_HPLIST(), ",");
                while (st.hasMoreTokens()) {
                    hpNumSet.add(st.nextToken().trim());
                }
                if (hpNumSet.size() == 0) {
                    resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_1001);
                    resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_1001_MSG + "(찾을 친구 연락처가 없습니다.)");
                    return responseJson(resultHeadMap, resultBodyMap, reqFriendModelBean.getAPPID());
                }

                List<UserInfoBean> userInfoBeans = redisUserService.multiGetUserInfoFromHpNumset(reqFriendModelBean.getAPPID(), hpNumSet);

                Map<String, String> makeFriendMap = new HashMap<String, String>();
                for (UserInfoBean userInfoBean : userInfoBeans) {
                    String userID = userInfoBean.getUSERID();
                    String hp_num = userInfoBean.getHP_NUM();
                    makeFriendMap.put(hp_num, userID);
                }

                resultBodyMap.put("friendMap", makeFriendMap);
            } else {
                return responseJson(resultHeadMap, resultBodyMap, reqFriendModelBean.getAPPID());
            }
        }catch (Exception e){
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap, reqFriendModelBean.getAPPID());
        }

        return responseJson(resultHeadMap,resultBodyMap, reqFriendModelBean.getAPPID());
    }

    private String responseJson(Map<String,String> resultHeadMap,Map<String,Object> resultBodyMap,String reqAPPID){

        Map<String,Object> returnRootMap = new HashMap<String, Object>();
        returnRootMap.put(Constants.HEADER_KEY, resultHeadMap);
        returnRootMap.put(Constants.BODY_KEY, resultBodyMap);
        returnRootMap.put(Constants.HEADER_SERVICE, reqAPPID);

        String responseJson = JsonObjectConverter.getJSONFromObject(returnRootMap);
        logger.debug("###[UserController] :" + responseJson);
        return responseJson;
    }
}
