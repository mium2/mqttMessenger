package com.msp.messenger.common.interceptor;

import com.msp.messenger.auth.MemoryTokenManager;
import com.msp.messenger.common.Constants;
import com.msp.messenger.service.redis.RedisUserService;
import com.msp.messenger.util.security.SecurityAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Time: 오후 3:18
 * To change this template use File | Settings | File Templates.
 */
public class CheckHandlerInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    // 보안인증을 클래스
    private static SecurityAuthorization sa = null;
    @Autowired(required = true)
    private Properties myProperties;

    protected Boolean isSecurity = false;
    @Autowired
    private MemoryTokenManager memoryTokenManager;

    @Autowired(required = true)
    RedisUserService redisUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        // 보안인증 클래스 생성
        if(sa==null) {
            sa = SecurityAuthorization.getInstance(myProperties);
        }
//        String requestUrlPath = requestURL.substring(requestURL.lastIndexOf("/")+1).trim();
        Map<String,String> authResultMap = new HashMap<String, String>();
        authResultMap.put(Constants.RESULT_CODE_KEY,Constants.RESULT_CODE_OK);
        authResultMap.put(Constants.RESULT_MESSAGE_KEY,Constants.RESULT_MESSAGE_OK);
        // 보안을 사용 한다고 했을때
        if(isSecurity = Boolean.parseBoolean(myProperties.getProperty("auth.using").trim())){
            if(sa.isCheckAuthIP(request.getRemoteAddr())){
                if(request.getParameter("USERID")==null){
                    authResultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3013);
                    authResultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3013_MSG);
                    return true;
                }else {
                    logger.debug("IP 인증성공!");
                    authResultMap.put("USERID", request.getParameter("USERID"));
                    request.setAttribute("authResultMap", authResultMap);
                    return true;
                }
            }

            if(request.getParameter("APPID")==null){
                authResultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3003);
                authResultMap.put(Constants.RESULT_MESSAGE_KEY, Constants.ERR_3003_MSG);
                return true;
            }

            if(request.getParameter("AUTHKEY")==null){
                authResultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3002);
                authResultMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_3002_MSG);
                return true;
            }

            if(request.getParameter("DEVICEID")==null){
                authResultMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_3011);
                authResultMap.put(Constants.RESULT_MESSAGE_KEY,Constants.ERR_3011_MSG);
                return true;
            }
            authResultMap = memoryTokenManager.validateAccessToken( request.getParameter("AUTHKEY"), request.getParameter("APPID"), request.getParameter("DEVICEID"));
            // 토큰이 만료되면 Redis 로그인 테이블에서 삭제처리하여 브로커서버에서도 접속이 안되게 한다.
            if(authResultMap.get(Constants.RESULT_CODE_KEY).equals(Constants.ERR_3005)){
                redisUserService.rmLoginID(authResultMap.get("USERID"));
                authResultMap.remove("USERID");
            }
        }
        //클라이언트로 부터 받은 request parameter 맵으로 저장
        request.setAttribute("authResultMap",authResultMap);
        logger.debug("인증 결과 : ResultCode : "+authResultMap.get(Constants.RESULT_CODE_KEY)+"   메세지 : "+authResultMap.get(Constants.RESULT_MESSAGE_KEY));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
