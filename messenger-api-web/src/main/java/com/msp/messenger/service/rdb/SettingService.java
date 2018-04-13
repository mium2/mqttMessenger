package com.msp.messenger.service.rdb;

import com.msp.messenger.bean.settging.OrganizationBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Created by Y.B.H(mium2) on 17. 1. 18..
 */
@Service
public class SettingService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    private SqlSessionTemplate sqlSession;

    @Autowired(required = true)
    private Properties myProperties;


    public int regUser(OrganizationBean organizationBean) throws Exception{
        int applyRow = sqlSession.update("setting.inOrganization",organizationBean);
        return applyRow;
    }
}
