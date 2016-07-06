package com.msp.chat.server.storage.sqlite;

import com.msp.chat.server.bean.events.PublishEvent;
import com.msp.chat.server.bean.Subscription;
import com.msp.chat.server.storage.ISubscribeStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 */
public class SqliteSubscribeStore implements ISubscribeStore {
    Logger LOGGER = LoggerFactory.getLogger("server");
    private Connection connection = null;
    private Statement stmt = null;
    private String dbFileName;
    private boolean isOpened = false;

    private String ChkTableName = "TB_MQTT_SUBSCRIPTION";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch(Exception e) { e.printStackTrace(); }
    }

    public SqliteSubscribeStore(String databaseFileName) {
        this.dbFileName = databaseFileName;
        initialize(); // 테이블 생성 및 DB 초기화
    }

    public void initialize(){
        LOGGER.debug("### CHK TableName :" + ChkTableName);
        try {
            if(!isExistTable(ChkTableName)){ //테이블 없을 경우 테이블 생성
                createTable(); //테이블 생성
            }
        }catch (Exception e){
            createTable(); //테이블 생성
//            e.printStackTrace();
        }
    }
    //사용자 아이디를 Key로 보내지 못한 메세지 리스트 Value를 만듬. 재접속시 한번에 미발송 메세지 전체발송 처리
    public void initPersistentMessageStore() {
        open(true);
        String sQuery = "select CLIENT_ID, TOPIC, QOS, MESSAGE, RETAIN_YN, MSG_ID ";
               sQuery += "from TB_MQTT_STOREDPUBLISHMSG ";
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sQuery);
            List<PublishEvent> storedPublishEvents = null;
            while(rs.next()){
                String sendClientID = rs.getString("CLIENT_ID");
                PublishEvent storedPublishEvent = new PublishEvent();
                storedPublishEvent.setPubClientID(sendClientID);
                storedPublishEvent.setM_topic(rs.getString("TOPIC"));
                storedPublishEvent.setPub_qos(rs.getInt("QOS"));
                storedPublishEvent.setPub_message(rs.getString("MESSAGE"));
                storedPublishEvent.setRetainYN(rs.getString("RETAIN_YN"));
                storedPublishEvent.setM_msgID(rs.getInt("MSG_ID"));

            }
            rs.close();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
    }

    public void initPersistentSubscriptions() {

    }


    public boolean open(boolean isReadOnly) {
        try {
            LOGGER.debug("### dbFileName:"+dbFileName);
            SQLiteConfig config = new SQLiteConfig();
            config.setReadOnly(isReadOnly); //읽기 전용일때 설정해 주는게 좋음
            this.connection = DriverManager.getConnection("jdbc:sqlite:/" + this.dbFileName, config.toProperties());
        } catch(SQLException e) { return false; }

        isOpened = true;
        return true;
    }

    public void close() {
        if(this.isOpened == false) {
            return;
        }
        try {
            this.connection.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewSubscription(Subscription newSubscription) {
        String clientID = newSubscription.getClientId();
        //DB 새로운 구독정보 관리
        String inSubSql = "insert into TB_MQTT_SUBSCRIPTION(CLIENT_ID,TOPIC,QOS,CLEANSESSION_YN,ACTIVY_YN) ";
               inSubSql += "values('"+clientID+"','"+newSubscription.getTopic()+"','"+newSubscription.getQos()+"','"+newSubscription.getCleanSessionYN()+"','"+newSubscription.getActiveYN()+"')";
        open(false);
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            stmt.execute(inSubSql);
            stmt.close();
            connection.commit();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
    }

    public void addNewSubscriptionList(List<Subscription> newSubscriptionList) {
        open(false);
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();
            for(Subscription newSubscription : newSubscriptionList){
                String clientID = newSubscription.getClientId();
                String inSubSql = "insert into TB_MQTT_SUBSCRIPTION(CLIENT_ID,TOPIC,QOS,CLEANSESSION_YN,ACTIVY_YN) ";
                inSubSql += "values('"+clientID+"','"+newSubscription.getTopic()+"','"+newSubscription.getQos()+"','"+newSubscription.getCleanSessionYN()+"','"+newSubscription.getActiveYN()+"')";
                stmt.execute(inSubSql);
            }
            stmt.close();
            connection.commit();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
    }

    public long getAllSubscribeCount(){
        int totalSubscribeCnt = 0;
        String selSql = "select count(*) as cnt from TB_MQTT_SUBSCRIPTION ";
        open(true);
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selSql);
            if(rs.next()){
                totalSubscribeCnt = rs.getInt("cnt");
            }
            rs.close();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
        return totalSubscribeCnt;
    }

    @Override
    public HashSet<String> getSubscribeClientID(String topic) throws Exception{
        return null;
    }

    public List<Subscription> getSearchSubscription(String clientID) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        String selSql = "select CLIENT_ID, TOPIC, QOS, CLEANSESSION_YN, ACTIVY_YN from TB_MQTT_SUBSCRIPTION ";
        selSql += " where CLIENT_ID='"+clientID+"'";

        open(true);
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selSql);
            while(rs.next()){
                Subscription subscription = new Subscription();
                subscription.setClientId(rs.getString("CLIENT_ID"));
                subscription.setTopic(rs.getString("TOPIC"));
                subscription.setQos(rs.getInt("QOS"));
                subscription.setCleanSessionYN(rs.getString("CLEANSESSION_YN"));
                subscription.setActiveYN(rs.getString("ACTIVY_YN"));

                subscriptions.add(subscription);
            }
            rs.close();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
        return subscriptions;
    }

    public void removeSubscription(String topic, String clientID){
        String delSql = "delete from TB_MQTT_SUBSCRIPTION where CLIENT_ID='"+clientID+"' and TOPIC='"+topic+"'";
        open(false);
        try {
            stmt = connection.createStatement();
            stmt.execute(delSql);
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
    }

    public void removeAllSubscriptions(String clientID) {
        String delSql = "delete from TB_MQTT_SUBSCRIPTION where CLIENT_ID='"+clientID+"'";
        open(false);
        try {
            stmt = connection.createStatement();
            stmt.execute(delSql);
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
    }

    public List<Subscription> retrieveAllSubscriptions() {
        List<Subscription> allSubscriptions = new ArrayList<Subscription>();
        String selSql = "select CLIENT_ID, TOPIC, QOS, CLEANSESSION_YN, ACTIVY_YN from TB_MQTT_SUBSCRIPTION";

        open(true);
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(selSql);
            while(rs.next()){
                Subscription subscription = new Subscription();
                subscription.setClientId(rs.getString("CLIENT_ID"));
                subscription.setTopic(rs.getString("TOPIC"));
                subscription.setQos(rs.getInt("QOS"));
                subscription.setCleanSessionYN(rs.getString("CLEANSESSION_YN"));
                subscription.setActiveYN(rs.getString("ACTIVY_YN"));

                allSubscriptions.add(subscription);
            }
            rs.close();
            stmt.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        close();
/*        for(int i=0; i<1000000; i++){
            Subscription subscription = new Subscription();
            subscription.setClientId("Test_"+i);
            subscription.setTopic("/topic");
            subscription.setQos(0);
            subscription.setCleanSessionYN("N");
            subscription.setActiveYN("N");

            allSubscriptions.add(subscription);
        }*/

        return allSubscriptions;
    }

    private boolean isExistTable(String tableName) throws Exception{
        open(true);
        if(!isOpened){
           throw new SQLException("DB를 여는데 실패");
        }
        String sQuery = "select count(*) from sqlite_master Where Name = '"+tableName+"'";
        PreparedStatement prep = this.connection.prepareStatement(sQuery);
        ResultSet row = prep.executeQuery();
        row.next();
        int tableCnt = row.getInt(1);
        row.close();
        prep.close();
        close();

        if(tableCnt>0){
            return true;
        }
        return false;
    }

    private void createTable(){
        open(false);
        try {
            connection.setAutoCommit(false);
            stmt = connection.createStatement();


            String cSql2 = "CREATE TABLE TB_MQTT_STOREDPUBLISHMSG ";
            cSql2+= "(STOREDMSG_IDX INTEGER  PRIMARY KEY AUTOINCREMENT,";
            cSql2+= "CLIENT_ID VARCHAR NOT NULL,";
            cSql2+= "MSG_ID INT NOT NULL,";
            cSql2+= "TOPIC VARCHAR NOT NULL,";
            cSql2+= "QOS INT DEFAULT 0,";
            cSql2+= "MESSAGE TEXT,";
            cSql2+= "RETAIN_YN CHAR(1) DEFAULT 'N')";
            stmt.execute(cSql2);

            String cSql3 = "CREATE TABLE TB_MQTT_SUBSCRIPTION ";
            cSql3+= "(CLIENT_ID VARCHAR NOT NULL,";
            cSql3+= "TOPIC VARCHAR NOT NULL,";
            cSql3+= "QOS INT DEFAULT 0,";
            cSql3+= "CLEANSESSION_YN CHAR(1) DEFAULT 'N',";
            cSql3+= "ACTIVY_YN CHAR(1) DEFAULT 'N',";
            cSql3+= "PRIMARY KEY (CLIENT_ID, TOPIC))";
            stmt.execute(cSql3);

            String createIndex3 = "CREATE INDEX SUBSCRIPTION_INDEX ON TB_MQTT_SUBSCRIPTION (CLIENT_ID)";
            stmt.execute(createIndex3);


            stmt.close();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close();
    }
}
