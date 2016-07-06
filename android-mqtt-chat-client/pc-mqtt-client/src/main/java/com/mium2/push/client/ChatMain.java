package com.mium2.push.client;

import com.mium2.push.client.commons.Constants;
import com.mium2.push.client.commons.MsgIDManager;
import kr.msp.upns.client.mqttv3.MqttClient;
import kr.msp.upns.client.mqttv3.MqttConnectOptions;
import kr.msp.upns.client.mqttv3.MqttException;
import kr.msp.upns.client.mqttv3.internal.MemoryPersistence;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Y.B.H(mium2) on 16. 6. 23..
 */
public class ChatMain {
    private MqttClient mqttClient = null;

    public static void main(String[] args) {
        System.out.println("=====================================================================");
        System.out.println("=========== Morpheus DBCP Encryption Tool ===================");
        System.out.println("STEP 1: 접속할 메신지서버 아이피를 입력하세요.");
        System.out.println("STEP 2: 접속할 메신지 서버 포트를 입력하세요.");
        System.out.println("STEP 3: 접속할 USERID를 입력하세요.");
        System.out.println("기타 : 파일전송=>#SYS_MSG_03=파일경로, 수신완료카운트조회=>#SYS_MSG01");
        System.out.println("=====================================================================");

        String command = "";

        while (StringUtils.isBlank(command)) {
            System.out.println("접속할 메신지서버 아이피를 입력하세요");
            command = getInput("BROKER IP");
        }
        String BROKER_IP = command;

        command = "";
        while (StringUtils.isBlank(command)) {
            System.out.println("접속할 메신지 서버 포트를 입력하세요");
            command = getInput("BROKER PORT");
        }
        String BROKER_PORT = command;

        command = "";
        while (StringUtils.isBlank(command)) {
            System.out.println("접속할 USERID를 입력하세요");
            command = getInput("CONNECT USERID");
        }
        String USERID = command;

        command = "";
        while (StringUtils.isBlank(command)) {
            System.out.println("대화방 아이디를 입력해주세요");
            command = getInput("CHAT ROOM ID");
        }
        String CHATROOM_ID = command;


        System.out.println("=====================================================================");
        System.out.println(" Connecting...");
        System.out.println("=====================================================================");
        //"tcp://211.241.199.139:1883";
        String connectUrl = "tcp://"+BROKER_IP+":"+BROKER_PORT;

        ChatMain chatmain = new ChatMain();
        if(chatmain.newConnect(connectUrl, USERID)) {
            chatmain.msgSend(CHATROOM_ID);
        }
    }

    private boolean newConnect(String connectUrl, String clientID){
        MqttClient mConnection = null;
        try {
            String BROKER_URL = connectUrl;
            int MQTT_KEEP_ALIVE = 60;

            mConnection = new MqttClient(BROKER_URL, clientID, new MemoryPersistence());
            mConnection.setCallback(new MessengerCallback(this));
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setCleanSession(false);
            mqttConnectOptions.setConnectionTimeout(10);
            mqttConnectOptions.setKeepAliveInterval(MQTT_KEEP_ALIVE);
            mConnection.connect(mqttConnectOptions);
            mqttClient = mConnection;
        }catch (MqttException me){
            //CONNECTION_ACCEPTED:0, UNNACEPTABLE_PROTOCOL_VERSION:1, IDENTIFIER_REJECTED:2
            //SERVER_UNAVAILABLE : 3, BAD_USERNAME_OR_PASSWORD:4, NOT_AUTHORIZED:5, ANOTHER_SERVER_CONNECT:6
            System.out.println("### 컨넥션 에러코드:"+me.getReasonCode());
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("#### 에러:"+e.getMessage());
            System.exit(-1);
        }
        return true;
    }

    private void msgSend(String CHATROOM_ID){

        String sendMsg = "";
        while (StringUtils.isBlank(sendMsg)) {
            sendMsg = getInput("보낼 메세지");
            if(!sendMsg.trim().equals("")) {
                try {
                    if(sendMsg.equals("help")){
                        helpMenu();
                    }else {
                        if (sendMsg.startsWith(Constants.SYS_MSG_PRIFIX)) {
                            if (sendMsg.startsWith(Constants.SYS_REQ_MSG_SENT_INFO)) {
                                //TODO : 수신확인 카운트 조회요청
                            } else if (sendMsg.startsWith(Constants.SYS_REQ_MSG_FILE)) {
                                //TODO : 파일전송
                                sendMsg = sendMsg.replace(Constants.SYS_REQ_MSG_FILE+"=","");
                                byte[] fileMsgBytes = fileSend(sendMsg);
                                mqttClient.publish(CHATROOM_ID, fileMsgBytes, 1, false, MsgIDManager.getInstance().getNextMessgeID());
                            }
                        } else {
                            mqttClient.publish(CHATROOM_ID, sendMsg.toString().getBytes(), 1, false, MsgIDManager.getInstance().getNextMessgeID());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            sendMsg = "";
        }

    }

    private static String getInput(String description) {
        if(description!=null) {
            System.out.print(description + ": ");
        }
        String input = null;

        InputStreamReader stream = null;
        BufferedReader reader = null;
        try {
            // Open a stream to stdin
            stream = new InputStreamReader(System.in);
            // Create a buffered reader to stdin
            reader = new BufferedReader(stream);
            // Try to read the string
            input = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    public void helpMenu(){
        System.out.println("=================== System Send Menu Command ========================");
        System.out.println("COMMAND : 파일전송             ex) #SYS_MSG03=/Users/mium2/project/java/MqttMessenger/temp_file/box_help.png");
        System.out.println("COMMAND : 수신완료카운트조회:     ex) #SYS_MSG01");
        System.out.println("=====================================================================");
    }

    private byte[] fileSend(String fileSrc){
        if(fileSrc.indexOf("/")>-1) {
            int lastIndex = fileSrc.lastIndexOf("/");
            String fileName = fileSrc.substring(lastIndex+1);

            try {
                FileInputStream sendFileInStream = new FileInputStream(fileSrc);
                FileChannel cin = sendFileInStream.getChannel();

                int fileSize = Constants.SYS_REQ_MSG_FILE.getBytes().length + 4 + fileName.getBytes().length + (int) cin.size();
                ByteBuffer buf = ByteBuffer.allocate(fileSize);
                buf.put(Constants.SYS_REQ_MSG_FILE.getBytes());
                buf.putInt(fileName.getBytes().length);
                buf.put(fileName.getBytes());
                cin.read(buf);
                buf.flip();
                byte[] sendFileByte = buf.array();
                cin.close();
                return sendFileByte;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
