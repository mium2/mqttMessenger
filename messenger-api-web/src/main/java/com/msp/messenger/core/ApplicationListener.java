package com.msp.messenger.core;

import com.msp.messenger.auth.MemoryTokenStorage;
import com.msp.messenger.auth.OAuth2Scope;
import com.msp.messenger.auth.vo.ClientVO;
import com.msp.messenger.bean.AppLicenseBean;
import com.msp.messenger.license.LicenseValidator;
import com.msp.messenger.service.redis.RedisAllocateUserService;

import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Y.B.H(mium2) on 16. 6. 16..
 */
public class ApplicationListener implements ServletContextListener {
    private static Logger logger = LoggerFactory.getLogger("com.msp.messenger.core.ApplicationListener.class");
    /** context */
    public static ServletContext sc;
    public static WebApplicationContext wContext;
    public static SqlSessionTemplate systemSqlMapClient;
    public static Properties webProperties = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.info("## [Web Application Starting...Wait...Please~~!]");
        sc = servletContextEvent.getServletContext();
        //Spring Application Context 가져오기
        wContext = WebApplicationContextUtils.getWebApplicationContext(sc);
        webProperties = (Properties)wContext.getBean("myProperties");
        systemSqlMapClient = (SqlSessionTemplate)wContext.getBean("sqlSession");

        // 라이센스 체크
        try {
            MemoryTokenStorage memoryTokenManager = (MemoryTokenStorage)wContext.getBean("memoryTokenStorage");

            LicenseValidator.getInstance().initialize();
            Map<String,AppLicenseBean> appLicenseBeanMap = LicenseValidator.getInstance().getAppLicenseBeanMap();
            // OAuth2 클라이언트 앱등록
            Set<Map.Entry<String,AppLicenseBean>> appLicenseBeanSet = appLicenseBeanMap.entrySet();
            for(Map.Entry<String,AppLicenseBean> mapEntry : appLicenseBeanSet){
                AppLicenseBean appLicenseBean = mapEntry.getValue();
                ClientVO clientVO = new ClientVO();
                clientVO.setCLEINT_SECRET(appLicenseBean.getMPSN_KEY());
                clientVO.setCLIENT_ID(appLicenseBean.getAPPID());
                clientVO.setCLIENT_TYPE("M");
                clientVO.setSCOPE(OAuth2Scope.ROLE_SERVICE_MASTER);
                memoryTokenManager.insertClient(clientVO);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        // 구동 시 RDB 브로커서버 정보 로드 및 설정 체크
        RedisAllocateUserService redisAllocateUserService = RedisAllocateUserService.getInstance();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("## Web Application Destroyed ~~ !!");
    }
}
