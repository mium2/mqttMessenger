package com.msp.messenger.service.rdb;

import com.msp.messenger.bean.UserInfoBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Created by Y.B.H(mium2) on 16. 6. 13..
 */
@Service
public class RdbUserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired(required = true)
    private SqlSessionTemplate sqlSession;

    @Autowired(required = true)
    private Properties myProperties;

    public int regUser(UserInfoBean userInfoBean) throws Exception{
        int applyRow = sqlSession.update("query.inUser",userInfoBean);
        return applyRow;
    }

    public int updateUser(UserInfoBean userInfoBean) throws Exception{
        int applyRow = sqlSession.update("query.upUser",userInfoBean);
        return applyRow;
    }

    public int delUser(UserInfoBean userInfoBean) throws Exception {
        int applyRow = sqlSession.delete("query.delUser",userInfoBean);
        return applyRow;
    }


}
