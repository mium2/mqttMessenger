package com.msp.messenger.util;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-11-05
 * Time: 오후 1:01
 * To change this template use File | Settings | File Templates.
 */
public class LogbackXmlLoader {
    private static LogbackXmlLoader instance = null;
    private String SentLogSrc = "";
    private String SysSentLogSrc = "";

    private LogbackXmlLoader() throws Exception{
        XMLConfiguration xmlConfiguration=null;

        Resource res=new ClassPathResource("logback.xml");
        xmlConfiguration=new XMLConfiguration(res.getFile());
        xmlConfiguration.setEncoding("UTF-8");
        xmlConfiguration.setAutoSave(true);
//            sentLogSrc:  C:/home/uracle/receiver/logs/sent/sent.%d{yyyy-MM-dd}.csv
//            sysSentLogSrc:  C:/home/uracle/receiver/logs/sent/sent.%d{yyyy-MM-dd}.csv
        List<HierarchicalConfiguration> appenders = xmlConfiguration.configurationsAt("appender");
        SubnodeConfiguration subnodeConfiguration = null;
        for (HierarchicalConfiguration append : appenders) {
            String appendName = append.getString("[@name]");
            if(appendName.equals("SENTAPPENDER")){
                subnodeConfiguration = append.configurationAt("rollingPolicy");
                SentLogSrc = subnodeConfiguration.getString("fileNamePattern");
            }else if(appendName.equals("SYSSENTAPPENDER")){
                subnodeConfiguration = append.configurationAt("rollingPolicy");
                SysSentLogSrc = subnodeConfiguration.getString("fileNamePattern");
            }
        }
    }

    public static LogbackXmlLoader getInstance() throws Exception{
        if(instance==null){
            instance = new LogbackXmlLoader();
        }
        return instance;
    }

    public String getSentLogSrc(){
        return SentLogSrc;
    }

    public String getSysSentLogSrc() {
        return SysSentLogSrc;
    }
}
