package com.msp.chat.server.commons.utill;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by mium2 on 15. 8. 19..
 */
public class BrokerConfig {
    public final static String SYS_MSG_PRIFIX="#SYS_MSG";
    public final static String SYS_MSG_SENT_COMPLETE="#SYS_MSG00";
    public final static String SYS_REQ_MSG_SENT_INFO="#SYS_MSG01";
    public final static String SYS_RES_MSG_SENT_INFO="#SYS_MSG02";
    public final static String SYS_REQ_MSG_FILE = "#SYS_MSG03";
    public final static String SYS_RES_MSG_FILE = "#SYS_MSG04";

    public final static String SYSTEM_BROKER_CLIENT_PRIFIX = "#SYSTEM#@";
    private static ConcurrentMap<String, String> properties = new ConcurrentHashMap<String, String>();
    public static String APPID = "appID";
    public static String SERVER_ID = "server_id";
    public static String MESSENGER_WEB_API_SERVER_HOST = "messenger-web-api-server-host";
    public static String OFFMSG_PUSH_SEND = "offmsg-push-send";
    public static String PUSH_UPMC_HOST = "push-upmc-host";
    public static String PUSH_SERVICECODE = "push-servicecode";
    public static String PUSH_SENDERCODE = "push-sendercode";
    public static String PORT = "port";
    public static String SSL_PORT = "ssl_port";
    public static String SSL_CIPHER_SUITE = "ssl_cipher_suite";
    public static String HTTP_PORT = "http_port";

    public static String OFFMSG_STORE_KIND = "offmsg_store_kind";

    public static String SUBSCRIBE_STORE_KIND = "scribeStore_kind";
    public static String SYNC_BROKER_SERVER_IP = "sync_broker_server_ip";
    public static String PROTOCOL_THREAD_COUNT = "protocol_thread_count";

    public static String MESSAGE_SIZE_LIMIT = "message_size_limit";
    public static String QUEUE_SIZE = "queue_size";
    public static String CACHE_USER_MSG_COUNT = "cache_user_msg_count";
    public static String MAX_MESSAGE_QUEUE_SIZE = "max_message_queue_size";
    public static String OFFMSG_CHECK_INTERVAL = "offmsg_check_minute_interval";
    public static String OFFMSG_EXPIRE_SECOND = "offmsg_expire_second";
    public static String LOCALE = "locale";

    public static String PASSWORD_FILE="password_file";
    public static String SQLITE_SRC="sqlite_scr";
    public static String ONEUSERMULTIDEVICE = "oneUserMultiDevice";
    public static String DBSTORAGEUSE_YN="dbStorageUseYN";

    public static String ROOM_MSG_MAX_SAVE_CNT="room_msg_max_save_cnt";

    public static String FILE_SAVE_SRC="file_save_src";
    public static String DOWNLOAD_HOSTURL="download_hosturl";
    public static String THUMBNAIL_WIDTH="thumbnail_width";
    public static String THUMBNAIL_HEIGHT="thumbnail_height";

    public static String ORG_PUB_MSG_CLEAN_TIME="org_pub_msg_clean_time";

    public static void Load(String filename) throws ConfigurationException {
        //기본 delimiter 가 , 여서 하는 설정
        XMLConfiguration.setDefaultListDelimiter((char) (0));
        XMLConfiguration xml = new XMLConfiguration(filename);

        properties.put(APPID, xml.getString(APPID,""));
        properties.put(SERVER_ID, xml.getString(SERVER_ID,""));
        properties.put(MESSENGER_WEB_API_SERVER_HOST, xml.getString(MESSENGER_WEB_API_SERVER_HOST,""));
        properties.put(OFFMSG_PUSH_SEND, xml.getString(OFFMSG_PUSH_SEND,"Y"));
        properties.put(PUSH_UPMC_HOST, xml.getString(PUSH_UPMC_HOST,""));
        properties.put(PUSH_SERVICECODE, xml.getString(PUSH_SERVICECODE,""));
        properties.put(PUSH_SENDERCODE, xml.getString(PUSH_SENDERCODE,""));
        properties.put(PORT, xml.getString(PORT,"1883"));
        properties.put(SSL_PORT, xml.getString(SSL_PORT,"8883"));
        properties.put(SSL_CIPHER_SUITE, xml.getString(SSL_CIPHER_SUITE,""));
        properties.put(HTTP_PORT, xml.getString(HTTP_PORT,"8888"));

        properties.put(OFFMSG_STORE_KIND, xml.getString(OFFMSG_STORE_KIND,"1"));

        properties.put(SUBSCRIBE_STORE_KIND, xml.getString(SUBSCRIBE_STORE_KIND,"redis"));
        properties.put(SYNC_BROKER_SERVER_IP, xml.getString(SYNC_BROKER_SERVER_IP,"0.0.0.0"));
        properties.put(PROTOCOL_THREAD_COUNT, xml.getString(PROTOCOL_THREAD_COUNT,"5"));

        properties.put(MESSAGE_SIZE_LIMIT, xml.getString(MESSAGE_SIZE_LIMIT,"0"));
        properties.put(QUEUE_SIZE, xml.getString(QUEUE_SIZE,"0"));
        properties.put(CACHE_USER_MSG_COUNT, xml.getString(CACHE_USER_MSG_COUNT,"10") );
        properties.put(MAX_MESSAGE_QUEUE_SIZE, xml.getString(MAX_MESSAGE_QUEUE_SIZE,"10000") );
        properties.put(OFFMSG_CHECK_INTERVAL, xml.getString(OFFMSG_CHECK_INTERVAL,"60") );
        properties.put(OFFMSG_EXPIRE_SECOND, xml.getString(OFFMSG_EXPIRE_SECOND,"86400") );
        properties.put(LOCALE, xml.getString(LOCALE,"en_US"));
        properties.put(PASSWORD_FILE, xml.getString(PASSWORD_FILE,""));
        properties.put(SQLITE_SRC, xml.getString(SQLITE_SRC,""));
        properties.put(ONEUSERMULTIDEVICE, xml.getString(ONEUSERMULTIDEVICE,"Y"));
        properties.put(DBSTORAGEUSE_YN, xml.getString(DBSTORAGEUSE_YN,"N"));

        properties.put(ROOM_MSG_MAX_SAVE_CNT, xml.getString(ROOM_MSG_MAX_SAVE_CNT,"100"));

        properties.put(FILE_SAVE_SRC, xml.getString(FILE_SAVE_SRC,""));
        properties.put(DOWNLOAD_HOSTURL, xml.getString(DOWNLOAD_HOSTURL,""));
        properties.put(THUMBNAIL_WIDTH, xml.getString(THUMBNAIL_WIDTH,"100"));
        properties.put(THUMBNAIL_HEIGHT, xml.getString(THUMBNAIL_HEIGHT,"80"));

        properties.put(ORG_PUB_MSG_CLEAN_TIME, xml.getString(ORG_PUB_MSG_CLEAN_TIME,"3"));
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
