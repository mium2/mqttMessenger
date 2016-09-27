import com.msp.messenger.util.HexUtil;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-11-05
 * Time: 오후 2:01
 * To change this template use File | Settings | File Templates.
 */
public class TestMain {
    public static void main(String[] args){
//        StringBuilder sb = new StringBuilder();
//        sb.append("안녕하세요~! %CNAME%님 이번달 청구금액 %VAR1%원 주소:%VAR2% 학교종이 땡땡땡 %VAR3%  안녕하세요 이메세지는 테스트 메세지 입니다. 모든 사람들이 행복해 졌으면 좋겠습니다. 모두 수고하세요");
//        sb.append("우리들 마음에 빛이 있다면 %VAR4% 여름엔 여름엔 %VAR5% 파랄거예야 산도들도 나무도 파란잎에서 %VAR6% 파랗게 파랗게 .. 정말 길다");
//
//        String test = "aaa bbb+ccc!@#$%^&*";
//        //주의 확인 : Provider에서  전달 받은 값을 decode 하기 때문에 문제가 되는 +(%2B),%(%25)값만  urlencode값으로 바꾼다.
//        try {
//            test = URLEncoder.encode(test,"utf-8");
//            System.out.println("###  "+test);
////            test =test.replace("+", "%2B");
//            test = test.replace("+", " ");
//            test = URLDecoder.decode(test,"utf-8");
//            System.out.println("###  "+test);
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

//        new TestMain().testMatching(sb.toString());
//        new TestMain().testBuildString(sb.toString());
//
//        String jsonString="{\"PUSHKEY\":\"0_ashdflaksdhflkjasdfbsdfbsdhfoweruosdhfnvksdhfsdnfl\", \"CUID\":\"testusername_0\", \"CNAME\":\"GUEST_0\", \"APPID\":\"com.uracle.push.test\", \"PNSID\":\"UPNS\", \"PSID\":\"0_ashdflaksdhflkjasdfbsdfbsdhfoweruosdhfnvksdhfsdnflsdnflsdfhofslkdnfkljwjfslkdflsdhfldhlfshdfashdflaksdhflkjasdfbsdfbsdhfoweruosdhfnvksdhfsdnflsdnflsdfhofslkdnfkljwjfslkdflsd\"}";
//
//        BasicPushUserBean basicPushUserBean = JsonObjectConverter.getObjectFromJSON(jsonString, BasicPushUserBean.class);
//
//        System.out.println("#### "+basicPushUserBean.toString());

//        DateUtil.getMakeTimeStamp(8,0);

        new TestMain().makeChatRoomID();
    }


    public TestMain(){}


    private void makeChatRoomID(){
        TreeSet<Object> reqInviteUserIDTreeSet = new TreeSet<Object>();
        reqInviteUserIDTreeSet.add("TEST222222");
        reqInviteUserIDTreeSet.add("TEST111111");

        StringBuffer sb = new StringBuffer();
        for(Object userIDObj : reqInviteUserIDTreeSet){
            sb.append(userIDObj.toString());
        }
//        int sumByte = 0;
//        System.out.println("MD5값" + HexUtil.getMD5(sb.toString()));
//        System.out.println("SHA128" + HexUtil.getSHA1(sb.toString()));
//        char[] chatRoomChars = Hex.encodeHex(sb.toString().getBytes());
        String makeRoomID = HexUtil.getMD5(sb.toString());
        char[] hexRoomID = Hex.encodeHex(sb.toString().getBytes());

        System.out.println("##### makeRoomID:"+makeRoomID);

        String groupMakeRoomID = HexUtil.getMD5("TEST111111"+System.currentTimeMillis());
        byte[] TokenAsBytes = new byte[groupMakeRoomID.length() / 2];

        groupMakeRoomID = groupMakeRoomID.toUpperCase();


//        byte[] cuidByteArr = charRoomString.getBytes();
//        for (int i = 0; i < cuidByteArr.length; i++) {
//            int aaa = cuidByteArr[i];
//            System.out.println("#### aaa :"+aaa);
//            sumByte += aaa;
//        }
    }



    private void testMatching(String _pushMsg){
        long startTime = System.currentTimeMillis();
        Map<String,String> personInfoMap = new HashMap<String, String>();
        personInfoMap.put("CNAME","홍길동");
        personInfoMap.put("VAR1","5000");
        personInfoMap.put("VAR2","서울시 강남구 삼성동");
        personInfoMap.put("VAR3","동요");
        personInfoMap.put("VAR4","1");
        personInfoMap.put("VAR5","2");
        personInfoMap.put("VAR6","3");
        personInfoMap.put("VAR7", "4");
        personInfoMap.put("VAR8", "5");
        personInfoMap.put("VAR9", "6");

        for(int i=0; i<10; i++){
            String pushMsg = _pushMsg;
            String chgPushMsg = pushMsg.replaceAll("%CNAME%",personInfoMap.get("CNAME"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR1%",personInfoMap.get("VAR1"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR2%",personInfoMap.get("VAR2"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR3%",personInfoMap.get("VAR3"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR4%",personInfoMap.get("VAR4"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR5%",personInfoMap.get("VAR5"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR6%",personInfoMap.get("VAR6"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR7%",personInfoMap.get("VAR7"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR8%",personInfoMap.get("VAR8"));
            chgPushMsg = chgPushMsg.replaceAll("%VAR9%",personInfoMap.get("VAR9"));
//            System.out.println("#### "+chgPushMsg);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("##### 작업시간 " + (endTime-startTime));
    }

    private void testBuildString(String _pushMsg){
        long startTime = System.currentTimeMillis();
        Map<String,String> personInfoMap = new HashMap<String, String>();
        personInfoMap.put("CNAME","홍길동");
        personInfoMap.put("VAR1","5000");
        personInfoMap.put("VAR2","서울시 강남구 삼성동");
        personInfoMap.put("VAR3","동요");
        personInfoMap.put("VAR4","1");
        personInfoMap.put("VAR5","2");
        personInfoMap.put("VAR6","3");
        personInfoMap.put("VAR7","4");
        personInfoMap.put("VAR8","5");
        personInfoMap.put("VAR9", "6");

        List<String> unmutableWordList = new ArrayList<String>();
        List<String> mutableWordList = new ArrayList<String>();

        while(true){
            int startPos = _pushMsg.indexOf("%");
            String remaindPushMsg = _pushMsg.substring(startPos+1);
            int endPos = remaindPushMsg.indexOf("%");
            if(startPos>-1 && endPos>-1){
                String appendMsg = _pushMsg.substring(0, startPos);
                String replaceWord = remaindPushMsg.substring(0,endPos);
                _pushMsg = remaindPushMsg.substring(endPos+1);
                unmutableWordList.add(appendMsg);
                mutableWordList.add(replaceWord);
            }else{
                unmutableWordList.add(_pushMsg);
                break;
            }
        }

        for(int i=0; i<10; i++){
            StringBuilder sb = new StringBuilder();
            for(int j=0; j<unmutableWordList.size(); j++){
                sb.append(unmutableWordList.get(j));
                if(j<mutableWordList.size()) {
                    sb.append(personInfoMap.get(mutableWordList.get(j)));
                }
            }
//            System.out.println("###" + sb.toString());
        }


        long endTime = System.currentTimeMillis();
        System.out.println("##### 작업시간 " + (endTime-startTime));
    }

    class TestThread1 extends Thread{
        @Override
        public void run() {

        }
    }

    class TestThread2 extends Thread{
        @Override
        public void run() {

        }
    }

    class TestThread3 extends Thread{
        @Override
        public void run() {

        }
    }
}
