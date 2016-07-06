package com.msp.messenger.util.mybatis;

/**
 * Created by Y.B.H(mium2) on 2016. 1. 26..
 */
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service("myBatisSupport")
public class MyBatisSupport {

    @Autowired(required = false)
    @Qualifier("sqlSession")
    protected SqlSessionTemplate sqlSession;

    @Autowired
    ApplicationContext applicationContext;

    public com.msp.messenger.util.mybatis.MyBatisTransactionManager getTransactionManager() {
        return applicationContext.getBean(com.msp.messenger.util.mybatis.MyBatisTransactionManager.class);
    }
}