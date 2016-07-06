package com.msp.messenger.util;

import java.io.*;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-06-24
 * Time: 오후 1:50
 * To change this template use File | Settings | File Templates.
 */
public class PropertiesLoader {
    private Properties cf = new Properties();
    private static final PropertiesLoader singleton = new PropertiesLoader();
    private PropertiesLoader(){
        initConfig();
    }
    public static PropertiesLoader getInstance(){
        return singleton;
    }
    public Properties getConfig(){
        return cf;
    }
    private void initConfig(){
        String tempConfigSrc = PropertiesLoader.class.getResource("").getPath();
        String HomeSrc = "";
        int homeLen = tempConfigSrc.indexOf("WEB-INF");
        HomeSrc = tempConfigSrc.substring(0,homeLen);
        String configSrc = HomeSrc+"WEB-INF/classes/config/config.properties";
        InputStream is = null;
        try {
            is = new FileInputStream(configSrc);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            cf.load(is);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
