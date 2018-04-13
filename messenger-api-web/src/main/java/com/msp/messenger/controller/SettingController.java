package com.msp.messenger.controller;

import au.com.bytecode.opencsv.CSVReader;
import com.msp.messenger.bean.settging.OrganizationBean;
import com.msp.messenger.common.Constants;
import com.msp.messenger.util.JsonObjectConverter;
import com.msp.messenger.util.mybatis.MyBatisTransactionManager;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 17. 1. 18..
 */
@Controller
public class SettingController {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    @Autowired(required = true)
    private SqlSessionTemplate sqlSession;

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping(value = "/setOrganization.ctl",produces = "application/json; charset=utf8")
    public @ResponseBody
    String setOrganization(HttpServletRequest request, HttpServletResponse response){
        Map<String,String> resultHeadMap = new HashMap<String, String>();
        Map<String,Object> resultBodyMap = new HashMap<String, Object>();
        //RDB 트랜젝션
        MyBatisTransactionManager transaction = applicationContext.getBean(MyBatisTransactionManager.class);
        try {
            resultHeadMap = (Map<String, String>) request.getAttribute("authResultMap");
            if (resultHeadMap.get(Constants.RESULT_CODE_KEY).equals(Constants.RESULT_CODE_OK)) {  //인증에러가 아닐 경우만 비즈니스 로직 수행

                String organizationCsvSrc = "/Users/mium2/project/git_repository/mqttMessenger/messenger-api-web/src/main/resources/config/organization_sample.csv";
                CSVReader reader = new CSVReader(new FileReader(organizationCsvSrc));
                // UTF-8
                // CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(organizationCsvSrc), "UTF-8"), ',', '"', 1);
                String[] datas;

                transaction.start(); // RDB transaction 시작.
                while ((datas = reader.readNext()) != null) {
                    if(datas[0].startsWith("#") || datas.length!=7){
                        continue;
                    }
                    int k=0;
                    OrganizationBean organizationBean = new OrganizationBean();
                    organizationBean.setORGAN_IDX(Integer.parseInt(datas[k++]));
                    organizationBean.setORGAN_P_IDX(Integer.parseInt(datas[k++]));
                    organizationBean.setORGAN_NAME(datas[k++]);
                    organizationBean.setORGAN_NAME_EN(datas[k++]);
                    organizationBean.setDEPTH(Integer.parseInt(datas[k++]));
                    organizationBean.setORDERBY(Integer.parseInt(datas[k++]));
                    organizationBean.setDESCRIPTION(datas[k++]);

                    sqlSession.update("setting.inOrganization", organizationBean);
                }
                transaction.commit();

            } else {
                return responseJson(resultHeadMap, resultBodyMap);
            }
        }catch (Exception e){
            e.printStackTrace();
            resultHeadMap.put(Constants.RESULT_CODE_KEY, Constants.ERR_500);
            resultHeadMap.put(Constants.RESULT_MESSAGE_KEY, e.getMessage());
            return responseJson(resultHeadMap, resultBodyMap);
        }finally {
            transaction.end();
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
