package com.mium2.messenger.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Y.B.H(mium2) on 2016. 9. 22..
 */
public class ConfigLoader {

    private static ConcurrentMap<String, String> properties = new ConcurrentHashMap<String, String>();

    public static String REDIS_MASTER_HOST = "redis.master.host";
    public static String REDIS_MASTER_PORT = "redis.master.port";
    public static String REDIS_MASTER_DB = "redis.master.db";
    public static String REDIS_SLAVE_HOST = "redis.slave.host";
    public static String REDIS_SLAVE_PORT = "redis.slave.port";
    public static String REDIS_SLAVE_DB = "redis.slave.db";
    public static String REDIS_SENTINELS_USE = "redis.sentinels.use";
    public static String REDIS_SENTINELS_IPS = "redis.sentinels.ips";
    public static String REDIS_SENTINEL_PORTS = "redis.sentinels.ports";
    public static String CLIENT_MAKE_CNT = "common.client_make_cnt";
    public static String CLIENT_PREFIX = "common.clientid_prefix";
    public static String CHATROOM_MAKE_CNT= "common.chatroom_make_cnt";
    public static String CHATROOM_PREFIX = "common.chatroom_prefix";
    public static String BROKERID = "common.brokerid";
    public static String BROKERIP_PORT = "common.brokerIpPort";

    public static void Load(String filename) throws ConfigurationException {
        //기본 delimiter 가 , 여서 하는 설정
        XMLConfiguration.setDefaultListDelimiter((char) (0));
        XMLConfiguration xml = new XMLConfiguration(filename);

        properties.put(REDIS_MASTER_HOST, xml.getString(REDIS_MASTER_HOST,"localhost"));
        properties.put(REDIS_MASTER_PORT, xml.getString(REDIS_MASTER_PORT,"6379"));
        properties.put(REDIS_MASTER_DB, xml.getString(REDIS_MASTER_DB,"3"));
        properties.put(REDIS_SLAVE_HOST, xml.getString(REDIS_SLAVE_HOST,"localhost"));
        properties.put(REDIS_SLAVE_PORT, xml.getString(REDIS_SLAVE_PORT,"6379"));
        properties.put(REDIS_SLAVE_DB, xml.getString(REDIS_SLAVE_DB,"3"));
        properties.put(REDIS_SENTINELS_USE, xml.getString(REDIS_SENTINELS_USE,"N"));
        properties.put(REDIS_SENTINELS_IPS, xml.getString(REDIS_SENTINELS_IPS,"localhost"));
        properties.put(REDIS_SENTINEL_PORTS, xml.getString(REDIS_SENTINEL_PORTS, "26379"));

        properties.put(CLIENT_MAKE_CNT, xml.getString(CLIENT_MAKE_CNT,"100"));
        properties.put(CLIENT_PREFIX, xml.getString(CLIENT_PREFIX,"TESTUSER"));
        properties.put(CHATROOM_MAKE_CNT, xml.getString(CHATROOM_MAKE_CNT,"3"));
        properties.put(CHATROOM_PREFIX, xml.getString(CHATROOM_PREFIX,"ROOMID"));
        properties.put(BROKERID, xml.getString(BROKERID,"BROKER_1"));
        properties.put(BROKERIP_PORT, xml.getString(BROKERIP_PORT,"localhost:1883"));
    }

    public static int getIntProperty(String name) {
        return Integer.parseInt(properties.get(name));
    }

    public static String getProperty(String name) {
        return properties.get(name);
    }

    public static long getLongProperty(String name) {
        return Long.parseLong(properties.get(name));
    }
}
