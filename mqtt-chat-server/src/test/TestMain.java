import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.commons.utill.DebugUtils;
import com.msp.chat.server.config.ApplicationConfig;
import com.msp.chat.server.storage.redis.RedisStorageService;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.RedisClientInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 4. 11..
 */
public class TestMain {
	public final static String SERVER_CONF_FILE = "./config/config.xml";
	public final static String LOG_CONF_FILE = "./config/logback.xml";
	public static ApplicationContext ctx = null;
	public TestMain(){
		//Logger 설정
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(LOG_CONF_FILE);
		} catch (JoranException je) {
			je.printStackTrace();
			return;
		}
		//브로커서버 config 설정 로드
		try {
			BrokerConfig.Load(SERVER_CONF_FILE);
		}catch(ConfigurationException e) {
			return;
		}
		ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);  //스프링 Config 호출
	}

	public static void main(String[] args){

		TestMain testMain = new TestMain();
//        testMain.testSplit();
		testMain.redisTest();

	}

	private void redisTest(){
		TestRedisWrite testRedisWrite = new TestRedisWrite();
		testRedisWrite.start();
//		while(true){
//			try {
//				Thread.sleep(30000);
//				TestRedisWrite2 testRedisWrite2 = new TestRedisWrite2();
//				testRedisWrite2.start();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//
//			try {
//				Thread.sleep(3600000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		TestRedisWrite2 testRedisWrite2 = new TestRedisWrite2();
		testRedisWrite2.start();

		TestRedisWrite3 testRedisWrite3 = new TestRedisWrite3();
		testRedisWrite3.start();
	}

	class TestRedisWrite extends Thread{
		Object obj = ctx.getBean("masterRedisTemplate");
		RedisTemplate masterRedisTemplate = (RedisTemplate)obj;

		@Override
		public void run() {
			long startMiliTime = System.currentTimeMillis();
			String KeyTable = "H_TEST";
			String putMsg = "{\"aaa\":\"111\",\"bbb\":\"222\",\"ccc\":\"333\"}";
//			masterRedisTemplate.setEnableTransactionSupport(false);
			for(int i=0; i<100000; i++){
				masterRedisTemplate.opsForHash().put(KeyTable, "KEY_" + i, putMsg + "_" + i);
			}
			long ElapsedTime = System.currentTimeMillis()-startMiliTime;

			System.out.println("### 경과시간1 : "+ElapsedTime);
		}
	}

	class TestRedisWrite2 extends Thread{
		Object obj = ctx.getBean("masterRedisTemplate");
		RedisTemplate masterRedisTemplate = (RedisTemplate)obj;

		@Override
		public void run() {
			long startMiliTime = System.currentTimeMillis();
			String KeyTable = "H_TEST";
			String putMsg = "{\"aaa\":\"111\",\"bbb\":\"222\",\"ccc\":\"333\"}";
			for(int i=100000; i<200000; i++){
				masterRedisTemplate.opsForHash().put(KeyTable, "KEY_" + i, putMsg + "_" + i);
			}
			long ElapsedTime = System.currentTimeMillis()-startMiliTime;

			System.out.println("### 경과시간2 : "+ElapsedTime);
		}
	}

	class TestRedisWrite3 extends Thread{
		Object obj = ctx.getBean("masterRedisTemplate");
		RedisTemplate masterRedisTemplate = (RedisTemplate)obj;

		@Override
		public void run() {
			long startMiliTime = System.currentTimeMillis();
			String KeyTable = "H_TEST";
			String putMsg = "{\"aaa\":\"111\",\"bbb\":\"222\",\"ccc\":\"333\"}";
			for(int i=200000; i<300000; i++){
				masterRedisTemplate.opsForHash().put(KeyTable, "KEY_" + i, putMsg + "_" + i);
			}
			long ElapsedTime = System.currentTimeMillis()-startMiliTime;

			System.out.println("### 경과시간3 : "+ElapsedTime);
		}
	}

	private void testSplit(){
        String splitString = "/images";
        String[] arr1 = splitString.split("/");
        System.out.println("### arr1 length:"+arr1.length);
        System.out.println("### arr1 length:"+arr1.length +"  arr1[0]:"+arr1[0]+"    arr1[1]:"+arr1[1]);

        String splitString2 = "/images/";
        String[] arr2 = splitString2.split("/");
        System.out.println("### arr2 length:"+arr2.length +"  arr2[0]:"+arr2[0]+"    arr2[1]:"+arr2[1]);


        String splitString3 = "/images/etc";
        String[] arr3 = splitString3.split("/");
        System.out.println("### arr3 length:"+arr3.length);
        System.out.println("### arr3 length:"+arr3.length +"  arr3[0]:"+arr3[0]+"    arr3[1]:"+arr3[1]);

        String splitString4 = "/images/etc/";
        String[] arr4 = splitString4.split("/");
        System.out.println("### arr4 length:"+arr4.length);
        System.out.println("### arr4 length:"+arr4.length +"  arr4[0]:"+arr4[0]+"    arr4[1]:"+arr4[1]);
    }

	private void makeChatRoomID(){
        ArrayList<String> cuids = new ArrayList<String>();
        cuids.add("ymium2");
        cuids.add("b");
        cuids.add("ab");
        cuids.add("ba");
        cuids.add("ab");
        cuids.add("a");
        cuids.add("fjaklf;ie");
        cuids.add("aaaabbb");
        cuids.add("bbbbde");
        cuids.add("fjaklf;ie");
        cuids.add("1bb1111bbde");
        cuids.add("2222222222");
        cuids.add("12222222222");
        cuids.add("32222222222");
        cuids.add("42222222222");
        cuids.add("52222222222");
        cuids.add("62222222222");
        cuids.add("72222222222");
        cuids.add("82222222222");
        cuids.add("29222222222");

        StringBuffer sb = new StringBuffer();
        TreeSet<String> cuidSet = new TreeSet<String>(cuids);
        for(String cuid : cuidSet){
            sb.append(cuid);
            System.out.println("###### cuid :"+cuid);
        }

        int sumByte = 0;
        char[] chatRoomChars = Hex.encodeHex(sb.toString().getBytes());
        String charRoomString = new String(chatRoomChars);


        byte[] cuidByteArr = charRoomString.getBytes();
        for (int i = 0; i < cuidByteArr.length; i++) {
            int aaa = cuidByteArr[i] << i;
            sumByte += aaa;
        }

        System.out.println("####"+sumByte);

//        byte[] defaultBytes = sb.toString().getBytes();
//        StringBuffer hexString = new StringBuffer();
//        try{
//            MessageDigest md = MessageDigest.getInstance("SHA-1");
//            md.reset();
//            md.update(defaultBytes);
//            byte messageDigest[] = md.digest();
//            System.out.println(new String(messageDigest,"utf-8"));
//            hexString.append(Hex.encodeHexString(messageDigest));
//        } catch (Exception nsae){
//
//            nsae.printStackTrace();
//        }
//        System.out.println(hexString.toString());
    }

    public static synchronized int getCuidSumByte(String pushKey, String cuid)
    {
        int sumByte = 0;
        String req_cuid = cuid.trim();
        if (req_cuid.length() > 20) {
            req_cuid = req_cuid.substring(0, 20);
        }
        if (("GUEST".equals(req_cuid)) || ("guest".equals(req_cuid)))
        {
            String req_pushKey = pushKey.substring(0, 20);
            byte[] pushkeyByteArr = req_pushKey.getBytes();
            for (int i = 0; i < pushkeyByteArr.length; i++) {
                sumByte += pushkeyByteArr[i];
            }
        }
        else
        {
            byte[] cuidByteArr = req_cuid.getBytes();
            for (int i = 0; i < cuidByteArr.length; i++) {
                if (i < 8)
                {
                    int aaa = cuidByteArr[i] << i;
                    sumByte += aaa;
                }
                else
                {
                    sumByte += cuidByteArr[i];
                }
            }
        }
        return sumByte;
    }

	private void testException() throws Exception{
		throw new RuntimeException("Internal bad error, found m_clientIDs to null while it should be initialized, somewhere it's overwritten!!");
	}

	public void makeThumnail(){
		try {
            FileUtils.forceMkdir(new File("/Users/mium2/project/java/MqttMessenger/temp_file/thumb"));
			//썸네일 가로사이즈
			int thumbnail_width = 100;
			//썸네일 세로사이즈
			int thumbnail_height = 100;
			//원본이미지파일의 경로+파일명
			File origin_file_name = new File("/Users/mium2/project/java/MqttMessenger/temp_file/water_PNG3290.png");
            String fileExtention = "png";
			//생성할 썸네일파일의 경로+썸네일파일명

			File thumb_file_name = new File("/Users/mium2/project/java/MqttMessenger/temp_file/thumb/water_PNG3290.png");

			BufferedImage buffer_original_image = ImageIO.read(origin_file_name);
			int orgImgHeight = buffer_original_image.getHeight();
			int orgImgWidth = buffer_original_image.getWidth();

            int autoResizeWidth = (orgImgWidth*thumbnail_height)/orgImgHeight;
            BufferedImage buffer_thumbnail_image;
            if(fileExtention.equals("png")){
                buffer_thumbnail_image = new BufferedImage(autoResizeWidth, thumbnail_height, BufferedImage.TYPE_INT_ARGB);
            }else {
                buffer_thumbnail_image = new BufferedImage(autoResizeWidth, thumbnail_height, BufferedImage.TYPE_3BYTE_BGR);
            }
			Graphics2D graphic = buffer_thumbnail_image.createGraphics();
			graphic.drawImage(buffer_original_image, 0, 0, autoResizeWidth, thumbnail_height, null);
			ImageIO.write(buffer_thumbnail_image, fileExtention, thumb_file_name);
			System.out.println("썸네일 생성완료");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void makeChatRoom(){
        byte[] protocal_code = new byte[4];
        byte[] dataLen = new byte[8];
        byte[] cuids = new byte[4*1024];

        String aaa = String.format("%c%-6d%c",protocal_code,dataLen,cuids);
        System.out.println("#### aaa : "+aaa);
	}

	public static int byteToInt(byte[] src){
		int s1 = src[0] & 0xFF;
		int s2 = src[1] & 0xFF;
		int s3 = src[2] & 0xFF;
		int s4 = src[3] & 0xFF;

		return ((s1 << 24)+(s2<<16)+(s3<<8)+(s4<<0));
	}

    public static byte[] intTobyte(int intVal){
        byte[] intToByte = new byte[4];
        int a = intVal;
        System.out.println("int를 byte 배열로 바꾼다.");
        intToByte[0] |= (byte)((a&0xFF000000)>>24);
        intToByte[1] |= (byte)((a&0xFF0000)>>16);
        intToByte[2] |= (byte)((a&0xFF00)>>8);
        intToByte[3] |= (byte)(a&0xFF);

        System.out.println("byte 배열을 int로 바꾼다");
        int result = 0;
        result |= (intToByte[0] & (int)0xFF)<<24;
        result |= (intToByte[1] & (int)0xFF)<<16;
        result |= (intToByte[2] & (int)0xFF)<<8;
        result |= (intToByte[3] & (int)0xFF);
        System.out.println("byte To Int is "+ result );

        return intToByte;
    }


	public String getHexString()throws UnsupportedEncodingException{
		byte[] raw = {
			(byte)255, (byte)254, (byte)253,
			(byte)252, (byte)251, (byte)250
		};

		final byte[] HEX_CHAR_TABLE = {
			(byte)'0', (byte)'1', (byte)'2', (byte)'3',
			(byte)'4', (byte)'5', (byte)'6', (byte)'7',
			(byte)'8', (byte)'9', (byte)'a', (byte)'b',
			(byte)'c', (byte)'d', (byte)'e', (byte)'f'
		};

		byte[] hex = new byte[2 * raw.length];
		int index = 0;

		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}

		return new String(hex, "ASCII");
	}

	public void testByteToString() {
		String example = "This is an example";
		byte[] bytes = example.getBytes();

		System.out.println("Text : "+example);
		System.out.println("Text [Byte format]: "+bytes);
		System.out.println("Text [Byte format] : " + bytes.toString());

		byte_to_ascii(bytes);
	}

	public void byte_to_ascii(byte[] b){
		System.out.println("Text [Ascii Format] :");
		for(int i=0; i<b.length; i++){
			System.out.println((int)b[i]+"  ");
		}
		System.out.println();
	}

	public void testByteBuffer(){
		String fileName = "icon_test01.png";
		String sendMsg = "안녕하세요. 이건 테스트 입니다.";
		int allocateSize = 4+fileName.getBytes().length + sendMsg.getBytes().length;


        byte[] testBytes = sendMsg.getBytes();
        ByteBuffer testByteBuffer = ByteBuffer.allocate(testBytes.length);
        testByteBuffer.put(testBytes);
//        testByteBuffer.flip();
        System.out.println("### testByteBuffer1:" + testByteBuffer + "    메세지 정보 : " + DebugUtils.payload2Str(testByteBuffer));
        testByteBuffer.position(0);
        System.out.println("### testByteBuffer2:" + testByteBuffer+"   testByteBuffer.remaining():"+testByteBuffer.remaining());
        byte[] outBytes = new byte[testByteBuffer.remaining()];
        testByteBuffer.get(outBytes);
        try {
            System.out.println("### testByteBuffer3:" + testByteBuffer + "    메세지 정보 : " + new String(outBytes,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        ByteBuffer buf = ByteBuffer.allocate(allocateSize);
		buf.putInt(fileName.length());
		buf.put(fileName.getBytes());
		buf.put(sendMsg.getBytes());
		buf.flip();
        System.out.println("### Step 1. : " + buf);
        System.out.println("===================================\n");
		try {
			int fileNamLen = buf.getInt();
			System.out.println("### Step 2 start :"+buf);
			System.out.println("#### fileNamLen:"+fileNamLen);
			System.out.println("#### Remaing size():" + buf.remaining());
			byte[] rev_fileNameByte = new byte[fileNamLen];
            buf.get(rev_fileNameByte);
            System.out.println("#### fileNameByteBuffer:" + new String(rev_fileNameByte, "utf-8"));
            System.out.println("### Step 2 end :"+buf);
            System.out.println("===================================\n");
            System.out.println("#### Step 3 start :" + buf);
            System.out.println("#### Remaing size():" + buf.remaining());
			byte[] rev_message = new byte[buf.remaining()];
			buf.get(rev_message);
            System.out.println("#### rev_message:" + new String(rev_message, "utf-8"));
			System.out.println("#### Step 3 end :" + buf);
			System.out.println("#### Remaing size():" + buf.remaining());
            System.out.println("===================================\n");
            buf.flip();
            System.out.println("#### Step 4 end :" + buf);
            System.out.println("===================================\n");
            ByteBuffer buf2 = buf.duplicate();
            System.out.println("#### Step 5 end :" + buf2);

		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public void testIntBuffer(){
		IntBuffer buf = IntBuffer.allocate(10);
		System.out.println(" 생성 된 후 :" + buf);

		for(int i=1; i<=5; i++){
			buf.put(i);
		}

		System.out.println("1에서 5까지 저장후 :" + buf);
		for(int i=0; i<10; i++){
			System.out.print(buf.get(i) + ",");
		}
		System.out.println("\n");

		buf.flip();
		System.out.println("flip():" + buf);
		System.out.println("remaining():" + buf.remaining());
		for(int i=0; i<5; i++){
			System.out.print(buf.get(i) + ",");
		}

		System.out.println("\n");
		buf.clear();
		System.out.println("clear():" + buf);
		System.out.println("remaining():" + buf.remaining());
		for(int i=0; i<10; i++){
			System.out.print(buf.get(i) + ",");
		}

		buf.position(5);
		System.out.println("버퍼의 pos를 5로 설정한 후 :" + buf);
		for(int i=101; i<=105; i++){
			buf.put(i);
		}
		buf.rewind();
		while(buf.hasRemaining()){
			System.out.print(buf.get()+",");
		}
	}

	public void StringToByte(){
		final byte[] CERT_BYTES = { (byte) 254, (byte) 237,
				(byte) 254, (byte) 237, (byte) 0, (byte) 0, (byte) 0, (byte) 2,
				(byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0,
				(byte) 0, (byte) 1, (byte) 0, (byte) 12, (byte) 115, (byte) 101,
				(byte) 99, (byte) 117, (byte) 114, (byte) 101, (byte) 115,
				(byte) 111, (byte) 99, (byte) 107, (byte) 101, (byte) 116,
				(byte) 0, (byte) 0, (byte) 1, (byte) 69, (byte) 231, (byte) 201,
				(byte) 156, (byte) 140, (byte) 0, (byte) 0, (byte) 5, (byte) 0,
				(byte) 48, (byte) 130, (byte) 4, (byte) 252, (byte) 48, (byte) 14,
				(byte) 6, (byte) 10, (byte) 43, (byte) 6, (byte) 1, (byte) 4,
				(byte) 1, (byte) 42, (byte) 2, (byte) 17, (byte) 1, (byte) 1,
				(byte) 5, (byte) 0, (byte) 4, (byte) 130, (byte) 4, (byte) 232,
				(byte) 221, (byte) 18, (byte) 203, (byte) 171, (byte) 175,
				(byte) 82, (byte) 132, (byte) 227, (byte) 115, (byte) 143,
				(byte) 38, (byte) 191, (byte) 42, (byte) 202, (byte) 130,
				(byte) 171, (byte) 75, (byte) 6, (byte) 161, (byte) 120,
				(byte) 204, (byte) 61, (byte) 106, (byte) 160, (byte) 81, (byte) 9,
				(byte) 204, (byte) 153, (byte) 166, (byte) 38, (byte) 246,
				(byte) 13, (byte) 43, (byte) 19, (byte) 100, (byte) 132, (byte) 45,
				(byte) 90, (byte) 143, (byte) 1, (byte) 231, (byte) 182, (byte) 89,
				(byte) 228, (byte) 183, (byte) 17, (byte) 95, (byte) 129,
				(byte) 229, (byte) 42, (byte) 182, (byte) 126, (byte) 114,
				(byte) 76, (byte) 124, (byte) 123, (byte) 246, (byte) 152,
				(byte) 0, (byte) 141, (byte) 212, (byte) 111, (byte) 52,
				(byte) 243, (byte) 112, (byte) 31, (byte) 117, (byte) 124,
				(byte) 142, (byte) 24, (byte) 59, (byte) 198, (byte) 164,
				(byte) 253, (byte) 21, (byte) 177, (byte) 189, (byte) 74,
				(byte) 218, (byte) 110, (byte) 83, (byte) 154, (byte) 49,
				(byte) 186, (byte) 159, (byte) 173, (byte) 202, (byte) 94,
				(byte) 174, (byte) 183, (byte) 223, (byte) 119, (byte) 109,
				(byte) 110, (byte) 72, (byte) 93, (byte) 208, (byte) 195,
				(byte) 19, (byte) 89, (byte) 33, (byte) 34, (byte) 186, (byte) 12,
				(byte) 86, (byte) 156, (byte) 156, (byte) 210, (byte) 111,
				(byte) 110, (byte) 44, (byte) 106, (byte) 36, (byte) 67,
				(byte) 168, (byte) 7, (byte) 179, (byte) 244, (byte) 53,
				(byte) 134, (byte) 10, (byte) 86, (byte) 179, (byte) 34, (byte) 60,
				(byte) 184, (byte) 179, (byte) 162, (byte) 69, (byte) 24,
				(byte) 168, (byte) 100, (byte) 183, (byte) 206, (byte) 64,
				(byte) 4, (byte) 32, (byte) 66, (byte) 237, (byte) 228, (byte) 92,
				(byte) 6, (byte) 213, (byte) 141, (byte) 147, (byte) 198,
				(byte) 141, (byte) 216, (byte) 41, (byte) 0, (byte) 101, (byte) 65,
				(byte) 41, (byte) 185, (byte) 128, (byte) 229, (byte) 107,
				(byte) 25, (byte) 89, (byte) 148, (byte) 16, (byte) 194,
				(byte) 101, (byte) 100, (byte) 243, (byte) 147, (byte) 77,
				(byte) 230, (byte) 11, (byte) 151, (byte) 99, (byte) 124,
				(byte) 55, (byte) 195, (byte) 185, (byte) 30, (byte) 234,
				(byte) 83, (byte) 61, (byte) 109, (byte) 131, (byte) 156,
				(byte) 244, (byte) 133, (byte) 66, (byte) 39, (byte) 153, (byte) 9,
				(byte) 34, (byte) 218, (byte) 201, (byte) 143, (byte) 190,
				(byte) 127, (byte) 119, (byte) 102, (byte) 6, (byte) 83,
				(byte) 134, (byte) 96, (byte) 170, (byte) 79, (byte) 196,
				(byte) 214, (byte) 47, (byte) 215, (byte) 37, (byte) 250,
				(byte) 64, (byte) 8, (byte) 165, (byte) 203, (byte) 44, (byte) 53,
				(byte) 113, (byte) 147, (byte) 251, (byte) 29, (byte) 26,
				(byte) 38, (byte) 193, (byte) 11, (byte) 223, (byte) 212,
				(byte) 114, (byte) 96, (byte) 162, (byte) 39, (byte) 48,
				(byte) 200, (byte) 172, (byte) 182, (byte) 254, (byte) 180,
				(byte) 198, (byte) 11, (byte) 128, (byte) 75, (byte) 74, (byte) 93,
				(byte) 226, (byte) 157, (byte) 80, (byte) 14, (byte) 9, (byte) 217,
				(byte) 236, (byte) 205, (byte) 153, (byte) 35, (byte) 242,
				(byte) 130, (byte) 140, (byte) 25, (byte) 16, (byte) 156,
				(byte) 247, (byte) 230, (byte) 5, (byte) 247, (byte) 0, (byte) 34,
				(byte) 196, (byte) 15, (byte) 118, (byte) 255, (byte) 185,
				(byte) 199, (byte) 59, (byte) 99, (byte) 27, (byte) 187, (byte) 83,
				(byte) 81, (byte) 12, (byte) 71, (byte) 69, (byte) 127, (byte) 130,
				(byte) 164, (byte) 97, (byte) 195, (byte) 216, (byte) 215,
				(byte) 61, (byte) 29, (byte) 196, (byte) 62, (byte) 160,
				(byte) 188, (byte) 209, (byte) 173, (byte) 230, (byte) 0,
				(byte) 204, (byte) 225, (byte) 1, (byte) 5, (byte) 42, (byte) 223,
				(byte) 232, (byte) 187, (byte) 190, (byte) 67, (byte) 126,
				(byte) 235, (byte) 178, (byte) 218, (byte) 179, (byte) 46,
				(byte) 186, (byte) 156, (byte) 186, (byte) 6, (byte) 191,
				(byte) 68, (byte) 239, (byte) 31, (byte) 16, (byte) 204, (byte) 24,
				(byte) 68, (byte) 164, (byte) 88, (byte) 10, (byte) 174, (byte) 26,
				(byte) 54, (byte) 187, (byte) 149, (byte) 132, (byte) 128,
				(byte) 173, (byte) 165, (byte) 8, (byte) 69, (byte) 96, (byte) 49,
				(byte) 57, (byte) 223, (byte) 110, (byte) 29, (byte) 215,
				(byte) 98, (byte) 42, (byte) 15, (byte) 153, (byte) 228,
				(byte) 216, (byte) 61, (byte) 160, (byte) 230, (byte) 34,
				(byte) 40, (byte) 232, (byte) 136, (byte) 139, (byte) 140,
				(byte) 236, (byte) 251, (byte) 119, (byte) 242, (byte) 199,
				(byte) 167, (byte) 61, (byte) 141, (byte) 89, (byte) 29, (byte) 82,
				(byte) 114, (byte) 229, (byte) 198, (byte) 27, (byte) 133,
				(byte) 87, (byte) 0, (byte) 53, (byte) 69, (byte) 42, (byte) 91,
				(byte) 174, (byte) 82, (byte) 244, (byte) 160, (byte) 82,
				(byte) 142, (byte) 221, (byte) 106, (byte) 151, (byte) 241,
				(byte) 214, (byte) 64, (byte) 14, (byte) 28, (byte) 2, (byte) 3,
				(byte) 145, (byte) 143, (byte) 18, (byte) 165, (byte) 247,
				(byte) 178, (byte) 211, (byte) 16, (byte) 222, (byte) 76,
				(byte) 60, (byte) 119, (byte) 130, (byte) 199, (byte) 230,
				(byte) 229, (byte) 3, (byte) 22, (byte) 100, (byte) 135,
				(byte) 103, (byte) 60, (byte) 181, (byte) 191, (byte) 56,
				(byte) 249, (byte) 181, (byte) 169, (byte) 210, (byte) 25,
				(byte) 152, (byte) 201, (byte) 226, (byte) 119, (byte) 71,
				(byte) 204, (byte) 70, (byte) 220, (byte) 103, (byte) 46,
				(byte) 166, (byte) 125, (byte) 40, (byte) 86, (byte) 208,
				(byte) 114, (byte) 138, (byte) 24, (byte) 27, (byte) 219,
				(byte) 123, (byte) 161, (byte) 52, (byte) 14, (byte) 38,
				(byte) 244, (byte) 112, (byte) 238, (byte) 121, (byte) 90,
				(byte) 34, (byte) 157, (byte) 131, (byte) 53, (byte) 245,
				(byte) 162, (byte) 89, (byte) 188, (byte) 6, (byte) 202,
				(byte) 164, (byte) 130, (byte) 34, (byte) 232, (byte) 74,
				(byte) 45, (byte) 137, (byte) 164, (byte) 200, (byte) 197,
				(byte) 247, (byte) 64, (byte) 110, (byte) 122, (byte) 49,
				(byte) 116, (byte) 137, (byte) 253, (byte) 170, (byte) 232,
				(byte) 120, (byte) 26, (byte) 171, (byte) 228, (byte) 229,
				(byte) 49, (byte) 56, (byte) 56, (byte) 106, (byte) 110, (byte) 12,
				(byte) 109, (byte) 93, (byte) 105, (byte) 241, (byte) 196,
				(byte) 11, (byte) 18, (byte) 89, (byte) 108, (byte) 146,
				(byte) 224, (byte) 161, (byte) 181, (byte) 236, (byte) 74,
				(byte) 128, (byte) 24, (byte) 239, (byte) 22, (byte) 146, (byte) 0,
				(byte) 69, (byte) 182, (byte) 246, (byte) 43, (byte) 59,
				(byte) 208, (byte) 33, (byte) 48, (byte) 81, (byte) 0, (byte) 70,
				(byte) 225, (byte) 222, (byte) 122, (byte) 178, (byte) 138,
				(byte) 12, (byte) 207, (byte) 233, (byte) 164, (byte) 13,
				(byte) 176, (byte) 123, (byte) 95, (byte) 68, (byte) 238,
				(byte) 134, (byte) 66, (byte) 95, (byte) 194, (byte) 192,
				(byte) 225, (byte) 244, (byte) 14, (byte) 78, (byte) 53,
				(byte) 189, (byte) 217, (byte) 229, (byte) 203, (byte) 192,
				(byte) 34, (byte) 38, (byte) 169, (byte) 63, (byte) 239,
				(byte) 128, (byte) 172, (byte) 143, (byte) 75, (byte) 7,
				(byte) 237, (byte) 125, (byte) 179, (byte) 235, (byte) 229,
				(byte) 98, (byte) 8, (byte) 211, (byte) 237, (byte) 116, (byte) 75,
				(byte) 27, (byte) 211, (byte) 131, (byte) 245, (byte) 89,
				(byte) 150, (byte) 35, (byte) 49, (byte) 207, (byte) 113,
				(byte) 237, (byte) 114, (byte) 125, (byte) 134, (byte) 191,
				(byte) 110, (byte) 30, (byte) 119, (byte) 131, (byte) 175,
				(byte) 166, (byte) 201, (byte) 255, (byte) 200, (byte) 1,
				(byte) 126, (byte) 163, (byte) 172, (byte) 52, (byte) 118,
				(byte) 184, (byte) 221, (byte) 165, (byte) 167, (byte) 165,
				(byte) 20, (byte) 135, (byte) 32, (byte) 222, (byte) 188,
				(byte) 250, (byte) 64, (byte) 161, (byte) 67, (byte) 236,
				(byte) 212, (byte) 131, (byte) 44, (byte) 32, (byte) 70, (byte) 0,
				(byte) 24, (byte) 178, (byte) 83, (byte) 155, (byte) 145,
				(byte) 136, (byte) 131, (byte) 120, (byte) 181, (byte) 164,
				(byte) 155, (byte) 172, (byte) 41, (byte) 213, (byte) 164,
				(byte) 98, (byte) 169, (byte) 152, (byte) 184, (byte) 170,
				(byte) 107, (byte) 7, (byte) 21, (byte) 228, (byte) 175,
				(byte) 192, (byte) 238, (byte) 68, (byte) 197, (byte) 119,
				(byte) 228, (byte) 225, (byte) 156, (byte) 235, (byte) 241,
				(byte) 172, (byte) 171, (byte) 236, (byte) 128, (byte) 78,
				(byte) 117, (byte) 152, (byte) 123, (byte) 93, (byte) 156,
				(byte) 57, (byte) 238, (byte) 211, (byte) 188, (byte) 47,
				(byte) 62, (byte) 45, (byte) 127, (byte) 58, (byte) 38, (byte) 29,
				(byte) 131, (byte) 95, (byte) 85, (byte) 149, (byte) 112,
				(byte) 215, (byte) 207, (byte) 41, (byte) 201, (byte) 30,
				(byte) 149, (byte) 73, (byte) 245, (byte) 179, (byte) 176,
				(byte) 246, (byte) 203, (byte) 204, (byte) 252, (byte) 13,
				(byte) 98, (byte) 151, (byte) 93, (byte) 87, (byte) 241,
				(byte) 166, (byte) 46, (byte) 249, (byte) 148, (byte) 49,
				(byte) 141, (byte) 136, (byte) 49, (byte) 77, (byte) 250,
				(byte) 191, (byte) 157, (byte) 90, (byte) 84, (byte) 51,
				(byte) 129, (byte) 133, (byte) 66, (byte) 253, (byte) 99,
				(byte) 243, (byte) 34, (byte) 142, (byte) 197, (byte) 4,
				(byte) 126, (byte) 7, (byte) 217, (byte) 126, (byte) 205,
				(byte) 250, (byte) 141, (byte) 231, (byte) 225, (byte) 203,
				(byte) 171, (byte) 246, (byte) 201, (byte) 48, (byte) 96,
				(byte) 207, (byte) 74, (byte) 253, (byte) 120, (byte) 114,
				(byte) 163, (byte) 192, (byte) 24, (byte) 12, (byte) 10,
				(byte) 210, (byte) 94, (byte) 136, (byte) 152, (byte) 185,
				(byte) 109, (byte) 87, (byte) 35, (byte) 159, (byte) 238,
				(byte) 122, (byte) 200, (byte) 107, (byte) 103, (byte) 243,
				(byte) 250, (byte) 152, (byte) 68, (byte) 66, (byte) 170, (byte) 0,
				(byte) 134, (byte) 229, (byte) 168, (byte) 182, (byte) 30,
				(byte) 89, (byte) 240, (byte) 121, (byte) 106, (byte) 148,
				(byte) 142, (byte) 49, (byte) 242, (byte) 215, (byte) 233,
				(byte) 57, (byte) 120, (byte) 204, (byte) 180, (byte) 239,
				(byte) 199, (byte) 133, (byte) 255, (byte) 71, (byte) 3,
				(byte) 132, (byte) 228, (byte) 110, (byte) 66, (byte) 227,
				(byte) 122, (byte) 82, (byte) 118, (byte) 173, (byte) 218,
				(byte) 54, (byte) 99, (byte) 167, (byte) 154, (byte) 3, (byte) 189,
				(byte) 25, (byte) 123, (byte) 169, (byte) 42, (byte) 184,
				(byte) 59, (byte) 36, (byte) 131, (byte) 206, (byte) 248,
				(byte) 90, (byte) 32, (byte) 183, (byte) 86, (byte) 62, (byte) 149,
				(byte) 107, (byte) 243, (byte) 71, (byte) 197, (byte) 124,
				(byte) 155, (byte) 214, (byte) 91, (byte) 29, (byte) 81, (byte) 28,
				(byte) 115, (byte) 98, (byte) 130, (byte) 184, (byte) 135,
				(byte) 13, (byte) 191, (byte) 147, (byte) 43, (byte) 10,
				(byte) 178, (byte) 99, (byte) 165, (byte) 210, (byte) 87,
				(byte) 87, (byte) 148, (byte) 31, (byte) 198, (byte) 129,
				(byte) 32, (byte) 181, (byte) 3, (byte) 144, (byte) 61, (byte) 5,
				(byte) 166, (byte) 252, (byte) 73, (byte) 205, (byte) 230,
				(byte) 178, (byte) 162, (byte) 46, (byte) 56, (byte) 99, (byte) 77,
				(byte) 97, (byte) 236, (byte) 121, (byte) 157, (byte) 139,
				(byte) 153, (byte) 217, (byte) 171, (byte) 19, (byte) 68,
				(byte) 36, (byte) 14, (byte) 123, (byte) 249, (byte) 101,
				(byte) 127, (byte) 184, (byte) 123, (byte) 7, (byte) 124,
				(byte) 68, (byte) 98, (byte) 34, (byte) 139, (byte) 224,
				(byte) 173, (byte) 246, (byte) 196, (byte) 180, (byte) 70,
				(byte) 207, (byte) 168, (byte) 211, (byte) 255, (byte) 84,
				(byte) 0, (byte) 174, (byte) 11, (byte) 160, (byte) 155,
				(byte) 127, (byte) 228, (byte) 81, (byte) 226, (byte) 115,
				(byte) 142, (byte) 200, (byte) 107, (byte) 4, (byte) 204,
				(byte) 219, (byte) 192, (byte) 189, (byte) 56, (byte) 127,
				(byte) 184, (byte) 187, (byte) 161, (byte) 106, (byte) 62,
				(byte) 225, (byte) 211, (byte) 115, (byte) 30, (byte) 172,
				(byte) 191, (byte) 66, (byte) 25, (byte) 66, (byte) 235,
				(byte) 107, (byte) 41, (byte) 186, (byte) 40, (byte) 239,
				(byte) 173, (byte) 11, (byte) 247, (byte) 89, (byte) 79,
				(byte) 135, (byte) 86, (byte) 73, (byte) 77, (byte) 164, (byte) 34,
				(byte) 109, (byte) 236, (byte) 56, (byte) 198, (byte) 141,
				(byte) 87, (byte) 74, (byte) 172, (byte) 56, (byte) 24, (byte) 150,
				(byte) 233, (byte) 233, (byte) 165, (byte) 122, (byte) 201,
				(byte) 112, (byte) 232, (byte) 23, (byte) 12, (byte) 166,
				(byte) 128, (byte) 114, (byte) 139, (byte) 207, (byte) 233,
				(byte) 47, (byte) 220, (byte) 172, (byte) 175, (byte) 40,
				(byte) 109, (byte) 82, (byte) 142, (byte) 130, (byte) 177,
				(byte) 50, (byte) 127, (byte) 196, (byte) 106, (byte) 172,
				(byte) 178, (byte) 71, (byte) 178, (byte) 204, (byte) 99,
				(byte) 113, (byte) 33, (byte) 189, (byte) 188, (byte) 168,
				(byte) 76, (byte) 92, (byte) 230, (byte) 211, (byte) 239,
				(byte) 75, (byte) 71, (byte) 64, (byte) 197, (byte) 26, (byte) 222,
				(byte) 19, (byte) 213, (byte) 161, (byte) 144, (byte) 20,
				(byte) 126, (byte) 192, (byte) 156, (byte) 15, (byte) 113,
				(byte) 64, (byte) 73, (byte) 7, (byte) 241, (byte) 217, (byte) 127,
				(byte) 171, (byte) 199, (byte) 66, (byte) 32, (byte) 179, (byte) 4,
				(byte) 181, (byte) 93, (byte) 121, (byte) 193, (byte) 10,
				(byte) 169, (byte) 255, (byte) 152, (byte) 199, (byte) 95,
				(byte) 177, (byte) 227, (byte) 135, (byte) 21, (byte) 64,
				(byte) 203, (byte) 9, (byte) 79, (byte) 243, (byte) 114, (byte) 2,
				(byte) 201, (byte) 157, (byte) 180, (byte) 52, (byte) 193,
				(byte) 66, (byte) 34, (byte) 155, (byte) 52, (byte) 35, (byte) 93,
				(byte) 31, (byte) 96, (byte) 77, (byte) 12, (byte) 80, (byte) 195,
				(byte) 96, (byte) 247, (byte) 251, (byte) 237, (byte) 36,
				(byte) 170, (byte) 7, (byte) 3, (byte) 251, (byte) 243, (byte) 47,
				(byte) 180, (byte) 98, (byte) 207, (byte) 176, (byte) 106,
				(byte) 237, (byte) 114, (byte) 91, (byte) 229, (byte) 56,
				(byte) 94, (byte) 154, (byte) 32, (byte) 62, (byte) 240,
				(byte) 132, (byte) 4, (byte) 144, (byte) 227, (byte) 140,
				(byte) 137, (byte) 76, (byte) 15, (byte) 117, (byte) 82,
				(byte) 223, (byte) 168, (byte) 135, (byte) 33, (byte) 91,
				(byte) 173, (byte) 4, (byte) 245, (byte) 192, (byte) 95,
				(byte) 135, (byte) 22, (byte) 138, (byte) 89, (byte) 1, (byte) 14,
				(byte) 230, (byte) 143, (byte) 195, (byte) 93, (byte) 133,
				(byte) 194, (byte) 252, (byte) 188, (byte) 31, (byte) 39,
				(byte) 162, (byte) 59, (byte) 148, (byte) 219, (byte) 213,
				(byte) 179, (byte) 195, (byte) 165, (byte) 67, (byte) 68,
				(byte) 39, (byte) 178, (byte) 143, (byte) 192, (byte) 177,
				(byte) 221, (byte) 236, (byte) 63, (byte) 40, (byte) 205,
				(byte) 26, (byte) 81, (byte) 127, (byte) 5, (byte) 213, (byte) 192,
				(byte) 22, (byte) 147, (byte) 98, (byte) 207, (byte) 153, (byte) 8,
				(byte) 108, (byte) 75, (byte) 182, (byte) 148, (byte) 0,
				(byte) 151, (byte) 15, (byte) 178, (byte) 98, (byte) 145,
				(byte) 255, (byte) 213, (byte) 142, (byte) 63, (byte) 247,
				(byte) 42, (byte) 161, (byte) 246, (byte) 21, (byte) 128,
				(byte) 47, (byte) 248, (byte) 217, (byte) 70, (byte) 195,
				(byte) 151, (byte) 236, (byte) 73, (byte) 153, (byte) 230,
				(byte) 152, (byte) 217, (byte) 12, (byte) 189, (byte) 65,
				(byte) 85, (byte) 189, (byte) 204, (byte) 212, (byte) 161,
				(byte) 210, (byte) 217, (byte) 74, (byte) 75, (byte) 186,
				(byte) 122, (byte) 167, (byte) 149, (byte) 178, (byte) 202,
				(byte) 205, (byte) 246, (byte) 225, (byte) 225, (byte) 190,
				(byte) 56, (byte) 42, (byte) 162, (byte) 215, (byte) 107,
				(byte) 45, (byte) 121, (byte) 235, (byte) 195, (byte) 219,
				(byte) 22, (byte) 0, (byte) 0, (byte) 0, (byte) 1, (byte) 0,
				(byte) 5, (byte) 88, (byte) 46, (byte) 53, (byte) 48, (byte) 57,
				(byte) 0, (byte) 0, (byte) 2, (byte) 211, (byte) 48, (byte) 130,
				(byte) 2, (byte) 207, (byte) 48, (byte) 130, (byte) 1, (byte) 183,
				(byte) 160, (byte) 3, (byte) 2, (byte) 1, (byte) 2, (byte) 2,
				(byte) 4, (byte) 58, (byte) 247, (byte) 71, (byte) 185, (byte) 48,
				(byte) 13, (byte) 6, (byte) 9, (byte) 42, (byte) 134, (byte) 72,
				(byte) 134, (byte) 247, (byte) 13, (byte) 1, (byte) 1, (byte) 11,
				(byte) 5, (byte) 0, (byte) 48, (byte) 23, (byte) 49, (byte) 21,
				(byte) 48, (byte) 19, (byte) 6, (byte) 3, (byte) 85, (byte) 4,
				(byte) 3, (byte) 19, (byte) 12, (byte) 115, (byte) 101, (byte) 99,
				(byte) 117, (byte) 114, (byte) 101, (byte) 115, (byte) 111,
				(byte) 99, (byte) 107, (byte) 101, (byte) 116, (byte) 48,
				(byte) 32, (byte) 23, (byte) 13, (byte) 49, (byte) 52, (byte) 48,
				(byte) 53, (byte) 49, (byte) 48, (byte) 50, (byte) 48, (byte) 49,
				(byte) 56, (byte) 52, (byte) 48, (byte) 90, (byte) 24, (byte) 15,
				(byte) 50, (byte) 49, (byte) 49, (byte) 52, (byte) 48, (byte) 52,
				(byte) 49, (byte) 54, (byte) 50, (byte) 48, (byte) 49, (byte) 56,
				(byte) 52, (byte) 48, (byte) 90, (byte) 48, (byte) 23, (byte) 49,
				(byte) 21, (byte) 48, (byte) 19, (byte) 6, (byte) 3, (byte) 85,
				(byte) 4, (byte) 3, (byte) 19, (byte) 12, (byte) 115, (byte) 101,
				(byte) 99, (byte) 117, (byte) 114, (byte) 101, (byte) 115,
				(byte) 111, (byte) 99, (byte) 107, (byte) 101, (byte) 116,
				(byte) 48, (byte) 130, (byte) 1, (byte) 34, (byte) 48, (byte) 13,
				(byte) 6, (byte) 9, (byte) 42, (byte) 134, (byte) 72, (byte) 134,
				(byte) 247, (byte) 13, (byte) 1, (byte) 1, (byte) 1, (byte) 5,
				(byte) 0, (byte) 3, (byte) 130, (byte) 1, (byte) 15, (byte) 0,
				(byte) 48, (byte) 130, (byte) 1, (byte) 10, (byte) 2, (byte) 130,
				(byte) 1, (byte) 1, (byte) 0, (byte) 153, (byte) 113, (byte) 7,
				(byte) 44, (byte) 219, (byte) 76, (byte) 101, (byte) 226,
				(byte) 138, (byte) 96, (byte) 219, (byte) 60, (byte) 167,
				(byte) 138, (byte) 222, (byte) 6, (byte) 78, (byte) 169, (byte) 64,
				(byte) 188, (byte) 156, (byte) 190, (byte) 119, (byte) 16,
				(byte) 34, (byte) 228, (byte) 250, (byte) 253, (byte) 119,
				(byte) 75, (byte) 240, (byte) 60, (byte) 242, (byte) 52,
				(byte) 137, (byte) 146, (byte) 20, (byte) 130, (byte) 202,
				(byte) 226, (byte) 125, (byte) 19, (byte) 7, (byte) 34, (byte) 8,
				(byte) 61, (byte) 243, (byte) 202, (byte) 225, (byte) 206,
				(byte) 223, (byte) 53, (byte) 74, (byte) 56, (byte) 222, (byte) 47,
				(byte) 99, (byte) 235, (byte) 57, (byte) 73, (byte) 90, (byte) 198,
				(byte) 109, (byte) 104, (byte) 36, (byte) 255, (byte) 124,
				(byte) 57, (byte) 155, (byte) 248, (byte) 120, (byte) 56,
				(byte) 56, (byte) 38, (byte) 41, (byte) 216, (byte) 1, (byte) 216,
				(byte) 216, (byte) 100, (byte) 239, (byte) 79, (byte) 222,
				(byte) 34, (byte) 21, (byte) 182, (byte) 112, (byte) 136,
				(byte) 137, (byte) 16, (byte) 141, (byte) 15, (byte) 83, (byte) 94,
				(byte) 245, (byte) 36, (byte) 203, (byte) 178, (byte) 137,
				(byte) 159, (byte) 86, (byte) 220, (byte) 253, (byte) 112,
				(byte) 200, (byte) 50, (byte) 135, (byte) 215, (byte) 190,
				(byte) 21, (byte) 186, (byte) 84, (byte) 21, (byte) 96, (byte) 126,
				(byte) 253, (byte) 115, (byte) 209, (byte) 241, (byte) 94,
				(byte) 115, (byte) 219, (byte) 0, (byte) 25, (byte) 253,
				(byte) 209, (byte) 182, (byte) 118, (byte) 230, (byte) 10,
				(byte) 50, (byte) 131, (byte) 39, (byte) 249, (byte) 136,
				(byte) 11, (byte) 101, (byte) 192, (byte) 12, (byte) 210,
				(byte) 179, (byte) 237, (byte) 213, (byte) 68, (byte) 101,
				(byte) 58, (byte) 187, (byte) 255, (byte) 240, (byte) 164,
				(byte) 147, (byte) 72, (byte) 148, (byte) 227, (byte) 155,
				(byte) 88, (byte) 250, (byte) 101, (byte) 253, (byte) 87,
				(byte) 140, (byte) 168, (byte) 39, (byte) 163, (byte) 133,
				(byte) 150, (byte) 252, (byte) 226, (byte) 234, (byte) 52,
				(byte) 88, (byte) 40, (byte) 56, (byte) 23, (byte) 105, (byte) 236,
				(byte) 4, (byte) 113, (byte) 98, (byte) 4, (byte) 0, (byte) 117,
				(byte) 59, (byte) 77, (byte) 236, (byte) 135, (byte) 93, (byte) 54,
				(byte) 30, (byte) 6, (byte) 126, (byte) 90, (byte) 15, (byte) 105,
				(byte) 89, (byte) 216, (byte) 154, (byte) 72, (byte) 134,
				(byte) 209, (byte) 74, (byte) 197, (byte) 237, (byte) 51,
				(byte) 37, (byte) 33, (byte) 106, (byte) 50, (byte) 71, (byte) 134,
				(byte) 169, (byte) 173, (byte) 88, (byte) 111, (byte) 217,
				(byte) 117, (byte) 184, (byte) 97, (byte) 1, (byte) 38, (byte) 76,
				(byte) 112, (byte) 170, (byte) 190, (byte) 250, (byte) 96,
				(byte) 17, (byte) 45, (byte) 117, (byte) 183, (byte) 82,
				(byte) 155, (byte) 10, (byte) 53, (byte) 15, (byte) 214, (byte) 36,
				(byte) 134, (byte) 249, (byte) 146, (byte) 98, (byte) 99,
				(byte) 64, (byte) 158, (byte) 99, (byte) 227, (byte) 21, (byte) 92,
				(byte) 98, (byte) 90, (byte) 202, (byte) 214, (byte) 134,
				(byte) 233, (byte) 212, (byte) 149, (byte) 2, (byte) 3, (byte) 1,
				(byte) 0, (byte) 1, (byte) 163, (byte) 33, (byte) 48, (byte) 31,
				(byte) 48, (byte) 29, (byte) 6, (byte) 3, (byte) 85, (byte) 29,
				(byte) 14, (byte) 4, (byte) 22, (byte) 4, (byte) 20, (byte) 115,
				(byte) 110, (byte) 177, (byte) 165, (byte) 41, (byte) 26,
				(byte) 142, (byte) 198, (byte) 221, (byte) 63, (byte) 79,
				(byte) 252, (byte) 219, (byte) 159, (byte) 68, (byte) 102,
				(byte) 76, (byte) 153, (byte) 128, (byte) 164, (byte) 48,
				(byte) 13, (byte) 6, (byte) 9, (byte) 42, (byte) 134, (byte) 72,
				(byte) 134, (byte) 247, (byte) 13, (byte) 1, (byte) 1, (byte) 11,
				(byte) 5, (byte) 0, (byte) 3, (byte) 130, (byte) 1, (byte) 1,
				(byte) 0, (byte) 118, (byte) 55, (byte) 245, (byte) 122,
				(byte) 159, (byte) 155, (byte) 98, (byte) 122, (byte) 229,
				(byte) 186, (byte) 23, (byte) 207, (byte) 109, (byte) 225,
				(byte) 220, (byte) 74, (byte) 51, (byte) 218, (byte) 10,
				(byte) 115, (byte) 137, (byte) 103, (byte) 127, (byte) 28,
				(byte) 30, (byte) 184, (byte) 149, (byte) 249, (byte) 193,
				(byte) 206, (byte) 208, (byte) 181, (byte) 191, (byte) 128,
				(byte) 18, (byte) 208, (byte) 24, (byte) 132, (byte) 147,
				(byte) 184, (byte) 198, (byte) 82, (byte) 204, (byte) 183,
				(byte) 127, (byte) 87, (byte) 234, (byte) 136, (byte) 197,
				(byte) 34, (byte) 232, (byte) 124, (byte) 210, (byte) 2,
				(byte) 192, (byte) 69, (byte) 246, (byte) 25, (byte) 232,
				(byte) 162, (byte) 0, (byte) 157, (byte) 216, (byte) 194,
				(byte) 26, (byte) 207, (byte) 225, (byte) 169, (byte) 59,
				(byte) 246, (byte) 52, (byte) 51, (byte) 150, (byte) 210,
				(byte) 50, (byte) 118, (byte) 58, (byte) 154, (byte) 45,
				(byte) 128, (byte) 138, (byte) 47, (byte) 174, (byte) 83,
				(byte) 117, (byte) 18, (byte) 224, (byte) 9, (byte) 146,
				(byte) 180, (byte) 178, (byte) 22, (byte) 76, (byte) 82,
				(byte) 229, (byte) 16, (byte) 150, (byte) 127, (byte) 13,
				(byte) 122, (byte) 218, (byte) 159, (byte) 195, (byte) 232,
				(byte) 168, (byte) 206, (byte) 105, (byte) 82, (byte) 37,
				(byte) 252, (byte) 186, (byte) 223, (byte) 222, (byte) 7,
				(byte) 106, (byte) 87, (byte) 218, (byte) 89, (byte) 22,
				(byte) 252, (byte) 7, (byte) 177, (byte) 52, (byte) 180, (byte) 9,
				(byte) 16, (byte) 29, (byte) 57, (byte) 192, (byte) 209,
				(byte) 225, (byte) 155, (byte) 16, (byte) 219, (byte) 38,
				(byte) 90, (byte) 174, (byte) 152, (byte) 140, (byte) 252,
				(byte) 114, (byte) 133, (byte) 106, (byte) 24, (byte) 107,
				(byte) 227, (byte) 80, (byte) 166, (byte) 63, (byte) 47, (byte) 16,
				(byte) 15, (byte) 89, (byte) 242, (byte) 19, (byte) 87, (byte) 193,
				(byte) 250, (byte) 222, (byte) 223, (byte) 183, (byte) 61,
				(byte) 91, (byte) 17, (byte) 92, (byte) 35, (byte) 142, (byte) 44,
				(byte) 153, (byte) 135, (byte) 86, (byte) 97, (byte) 70,
				(byte) 205, (byte) 38, (byte) 192, (byte) 18, (byte) 244,
				(byte) 61, (byte) 46, (byte) 21, (byte) 145, (byte) 99, (byte) 72,
				(byte) 142, (byte) 37, (byte) 19, (byte) 219, (byte) 167,
				(byte) 62, (byte) 71, (byte) 197, (byte) 86, (byte) 152,
				(byte) 139, (byte) 122, (byte) 231, (byte) 122, (byte) 206,
				(byte) 42, (byte) 142, (byte) 164, (byte) 237, (byte) 19,
				(byte) 60, (byte) 95, (byte) 239, (byte) 191, (byte) 64,
				(byte) 188, (byte) 94, (byte) 154, (byte) 199, (byte) 252,
				(byte) 62, (byte) 26, (byte) 181, (byte) 194, (byte) 141,
				(byte) 13, (byte) 1, (byte) 112, (byte) 161, (byte) 195,
				(byte) 149, (byte) 116, (byte) 57, (byte) 118, (byte) 114,
				(byte) 248, (byte) 235, (byte) 54, (byte) 229, (byte) 48,
				(byte) 53, (byte) 30, (byte) 145, (byte) 199, (byte) 207,
				(byte) 49, (byte) 175, (byte) 44, (byte) 172, (byte) 120,
				(byte) 254, (byte) 181, (byte) 100, (byte) 113, (byte) 191,
				(byte) 64, (byte) 131, (byte) 125, (byte) 80, (byte) 180,
				(byte) 229, (byte) 109, (byte) 97, (byte) 8, (byte) 166,
				(byte) 155, (byte) 72, (byte) 252, (byte) 84, (byte) 62, (byte) 97,
				(byte) 80, (byte) 26, (byte) 17, (byte) 143, (byte) 96, (byte) 16,
				(byte) 204, (byte) 86, (byte) 61, (byte) 226, (byte) 149 };

		try {
			char[] buffer = new char[CERT_BYTES.length];
			CharBuffer cBuffer = ByteBuffer.wrap(CERT_BYTES).asCharBuffer();
			for(int i = 0; i < CERT_BYTES.length; i++) {
				cBuffer.put(buffer[i]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static byte[] stringToBytesUTFNIO(String str) {
		char[] buffer = str.toCharArray();
		byte[] b = new byte[buffer.length << 1];
		CharBuffer cBuffer = ByteBuffer.wrap(b).asCharBuffer();
		for(int i = 0; i < buffer.length; i++)
			cBuffer.put(buffer[i]);
		return b;
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

	public static byte[] intTobyte(int integer, ByteOrder order) {
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE / 8); //1byte => 8bit
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

	private void testPrint(){
		for(int i=0; i<100; i++){
			System.out.println("##### i :"+i);
			ByteBuffer byteBuffer = ByteBuffer.allocate(100/2);
		}

		while(true){
			// byte 배열을 int형로 바꿈<br>
			// bytes put하면 position과 limit는 같은 위치가 됨.
		}
	}
}
