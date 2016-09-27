package com.msp.messenger.service.mqttclient;

import com.msp.messenger.core.ApplicationListener;
import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.MqttSecurityException;
import kr.msp.upns.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Y.B.H(mium2) on 16. 9. 12..
 */
public class MessengerClientPoolMgr {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private static final MessengerClientPoolMgr instance = new MessengerClientPoolMgr();
    private Map<String,Vector<MqttClient>> poolsMap = new ConcurrentHashMap<String,Vector<MqttClient>>();
    private MessageIDGenerator m_messageIDGenerator = new MessageIDGenerator();
    public final static String SYSTEM_BROKER_CLIENT_PRIFIX = "#SYSTEM#@";
    private final String ServerID;
    private int maxConn = 50;
    private int count = 0;

    private MessengerClientPoolMgr(){
        this.ServerID = ApplicationListener.webProperties.getProperty("SERVERID");
    }

    public static MessengerClientPoolMgr getInstance(){
        return instance;
    }
    /**
     * Method to destroy all connections.
     */
    public void destroy() {
        closeAll();
    }
    /**
     * Method to add free connections in to pool.
     * @param mqttClient
     */
    public synchronized void freeConnection(String upnsID,MqttClient mqttClient) {
        if(poolsMap.containsKey(upnsID)) {
            Vector<MqttClient> pools = poolsMap.get(upnsID);
            pools.addElement(mqttClient);
            poolsMap.put(upnsID,pools);
            count--;
            notifyAll();
        }
    }
    /**
     * Method to get connections.
     *
     * @return Connection
     */
    public synchronized MqttClient getConnection(String upnsID, String url) {
        MqttClient connection = null;
        if(poolsMap.containsKey(upnsID)){ //해당 서버의 풀이 있는지 확인
            Vector<MqttClient> pools = poolsMap.get(upnsID);
            if(pools==null){ //해당서버의 만들어 진 풀이 없으므로 Vector풀 생성
                pools = new Vector<MqttClient>();
                connection = newConnection(url);
                logger.info("## [NEW UPNS CONNECTION CREATED]");
                poolsMap.put(upnsID,pools);
                return connection;
            }else{
                if (pools.size() > 0) { //기존에 연결되어 있는 컨넥션 리턴
                    connection = pools.elementAt(0);
                    pools.removeElementAt(0);
                    try {
                        if (!connection.isConnected()) {  // 연결된 클라이언트가 아닐 경우 다시 가져옴
                            connection = getConnection(upnsID,url);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        connection = getConnection(upnsID,url);
                    }
                    return connection;
                }else{  // 해당서버의 반환된 연결사용할수 있는 컨넥션이 하나도 없으면 새로 하나 생성
                    connection = newConnection(url);
                    return connection;
                }
            }
        }

        Vector<MqttClient> pools = new Vector<MqttClient>();
        connection = newConnection(url);

        logger.debug("upnsID:"+upnsID+"     connection:"+connection);

        poolsMap.put(upnsID,pools);
        return connection;
    }

    /**
     * Method to close all resources
     */
    public synchronized void closeAll() {
        Set<Map.Entry<String,Vector<MqttClient>>> s1 = poolsMap.entrySet();
        int i=0;
        for(Map.Entry<String,Vector<MqttClient>> me : s1){
            Vector pools = me.getValue();
            for (Enumeration enumeration = pools.elements(); enumeration.hasMoreElements();) {
                try {
                    MqttClient connection = (MqttClient) enumeration.nextElement();
                    if(connection.isConnected()){
                        connection.disconnect();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            pools.removeAllElements();
        }

    }
    /**
     * Method to create new MqttClient connection object.
     *
     * @return Connection.
     */
    private MqttClient newConnection(String url) {
        MqttClient connection = null;

        MemoryPersistence dataStore = new MemoryPersistence();
        int clientNo = m_messageIDGenerator.next();
        try {
            String connectClientID = SYSTEM_BROKER_CLIENT_PRIFIX+ServerID+"_"+clientNo;

            if(System.getProperty("UPNS_USE_SSL").equals("Y")){
                logger.debug("## [MQTT SSL Connection Create] ID:" + connectClientID + "Connect HOST: ssl://{}",url);
                connection = new MqttClient("ssl://" + url, connectClientID, dataStore);
            }else {
                logger.debug("## [MQTT TCP Connection Create] ID:" + connectClientID + "Connect HOST: tcp://{}",url);
                connection = new MqttClient("tcp://" + url, connectClientID, dataStore);
            }
            connection.setCallback(new BrokerCallback());
            MqttConnectOptions conOpt = new MqttConnectOptions();
            conOpt.setConnectionTimeout(3);
            conOpt.setKeepAliveInterval(90);
            conOpt.setCleanSession(true);
            try {
                connection.connect(conOpt);
            } catch (MqttSecurityException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } catch (MqttException e) {
            e.printStackTrace();
            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return connection;
    }

}
