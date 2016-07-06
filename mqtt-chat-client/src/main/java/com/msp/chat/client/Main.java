package com.msp.chat.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.chat.MakeChatRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */

public class Main{
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static boolean received;
    
    public static void main(String[] args)  throws IOException, InterruptedException {
        /*****************************************************************************
         *  Byte[] int 변환 테스트
         *****************************************************************************/
//        testByte();
        /*****************************************************************************
        *  클라이언트 쓰레드로 여러개 구동 시키기 테스트
        *****************************************************************************/
        List<String> pnsids = new ArrayList<String>();
        pnsids.add("UPNS200000");
        pnsids.add("UPNS200000");

        String clientID = "TEST001";
        String[] topics = {"A_TOPIC"};

        Client client = new Client("localhost", 1883);
        try {
            for(int i=0; i<1; i++) {
                client.connect(false, clientID);  //세션 클린 하지 않는다.
                Thread.sleep(1000);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }


        /*****************************************************************************
         *  Publish  Connect 테스트
         *****************************************************************************/
//        Client client = new Client("211.241.199.217", 1883);
//        try {
//            client.connect(false, "TestSender01");  //세션 클린 하지 않는다.
//        }catch (InterruptedException e){
//            e.printStackTrace();
//            System.exit(1);
//        }
//        String[] topics = {"topic","3f41e9b8dcb74ae39f9deb89007d51412fa963cb"};
//        String[] topics = {"3f41e9b8dcb74ae39f9deb89007d51412fa963cb"};

        /*****************************************************************************
         *  구독신청하기 후 메세지 콜백 등록
         *****************************************************************************/
        client.subscribe(topics, new IPublishCallback() {
            public void published(String topic, String revMsg) {
                System.out.println("################ 서버에서 들어온 구독 메세지:"+revMsg);
                received = true;
            }
        });

        /*****************************************************************************
         *  구독 신청 없이 메세지 콜백 테스트
         ****************************************************************************/
/*        client.getM_subscribersList().put("topic",new IPublishCallback() {
            public void published(String topic, String revMsg) {
                System.out.println("################ 서버에서 들어온 구독 메세지:"+revMsg);
                received = true;
            }
        });

        Thread.sleep(2000);*/

        /*****************************************************************************
         *  메세지 전송 방법 :1 구독/토픽을 이용한 토픽을 구독한 모든 사용자 발송
         *****************************************************************************/
        String userids = "aaa,bbb,ccc,ddd";
        String topic = "A_TOPIC";
        String payload = "안녕하세요. 테스트 메세지입니다.";
        for(int i=0; i<1; i++) {
            payload = payload+i;
            client.publish(topic, payload.getBytes(), AbstractMessage.QOSType.MOST_ONE, false);
        }
        System.out.println("###### 방법1:메세지 발송");

//        /*****************************************************************************
//         *  메세지 전송 방법 :1 구독/토픽을 이용한 토픽을 구독한 모든 사용자 발송
//         *****************************************************************************/
//        String userids = "aaa,bbb,ccc,ddd";
//        MakeChatRoom makeChatRoom = new MakeChatRoom("kr.msp.chat.test","222","두번째테스트방",userids);
//        client.publish(Constants.SYSTEM_TOPIC,makeChatRoom.getMakeChatRoomByte(), AbstractMessage.QOSType.MOST_ONE, false);
//        System.out.println("##### 푸시 발송 : "+makeChatRoom.toString());
//        client.publish(clientID, "테스트 메세지 보냅니다.".getBytes(), AbstractMessage.QOSType.MOST_ONE, true);
//        System.out.println("###### 방법1:메세지 발송");

        /*****************************************************************************
         *  메세지 전송 방법 :2  메세지를 전송할 사용자 배열을 이용한 발송
         *****************************************************************************/
/*
        List<String> revUsers = new ArrayList<String>();
        revUsers.add("client001");
        client.sendPushMsg(revUsers, "사용자 선택 직접 메세지 전달합니다.", AbstractMessage.QOSType.MOST_ONE, true);
        System.out.println("###### 방법2:메세지 발송");

*/

        /*****************************************************************************
         *  그룹구독신청하기 테스트
         *****************************************************************************/
/*        List<String> clients = new ArrayList<String>();
        for(int i=400000; i<500000; i++){
            clients.add("Client"+i);
        }
        client.addGroupSubscribe("TestGroup",clients);*/
//        client.close();
    }

    private byte[] getSendFileByteArr(String fileAbsSrc){
        try {
            String fileName = "box_help_back.png";
            FileInputStream sendFileInStream = new FileInputStream("/Users/mium2/project/java/MqttChat/temp_file/box_help.png");
            FileChannel cin = sendFileInStream.getChannel();
            int fileSize = 4+fileName.getBytes().length+(int) cin.size();
            ByteBuffer buf = ByteBuffer.allocate(fileSize);
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
        return null;
    }

    public static void testByte(){
        int testInt = 16;
//        for(int testInt=0; testInt<20; testInt++) {
            System.out.println("===== ByteOrder.LITTLE_ENDIAN  ===");
            byte[] bytes = intTobyte(testInt, ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < bytes.length; i++) {
                System.out.printf("[%02X]", bytes[i]);
            }
            System.out.println();
            System.out.println(byteToInt(bytes, ByteOrder.LITTLE_ENDIAN));

/*            System.out.println("===== ByteOrder.BIG_ENDIAN  ===");
            bytes = intTobyte(testInt, ByteOrder.BIG_ENDIAN);

            for (int i = 0; i < bytes.length; i++) {
                System.out.printf("[%02X]", bytes[i]);
            }
            System.out.println();
            System.out.println(byteToInt(bytes, ByteOrder.BIG_ENDIAN));*/
        }
//    }

    /**
     * int형을 byte배열로 바꿈<br>
     * @param integer
     * @param order
     * @return
     */
    public static byte[] intTobyte(int integer, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8); //1byte => 8bit
        buff.order(order);

        // 인수로 넘어온 integer을 putInt로설정
        buff.putInt(integer);

        System.out.println("intTobyte : " + buff);
        return buff.array();
    }

    /**
     * byte배열을 int형로 바꿈<br>
     * @param bytes
     * @param order
     * @return
     */
    public static int byteToInt(byte[] bytes, ByteOrder order) {

        ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8);
        buff.order(order);

        // buff사이즈는 4인 상태임
        // bytes를 put하면 position과 limit는 같은 위치가 됨.
        buff.put(bytes);
        // flip()가 실행 되면 position은 0에 위치 하게 됨.
        buff.flip();

        System.out.println("byteToInt : " + buff);

        return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
    }


}




