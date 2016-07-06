package com.msp.messenger.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author cronosalt
 *
 */
public class SEEDUtil {
	private static final String CHARACTER_SET = "UTF-8";
	private static final String DEFAULT_IV = "1234567890123456";

	public static byte [] getSeedDecrypt(byte [] cipher, int [] key) throws Exception {
		// 암호화문 byte 배열 리스트로 변환
		List<byte[]> encByteList = getByteList(cipher);

		//        System.out.println("복호화할 바이트 시작");
		//        for(int i=0; i<encByteList.size(); i++){
		//            byte[] originalByte = encByteList.get(i);
		//            for(int j=0; j<originalByte.length; j++){
		//                System.out.println("" + (j+(i*16)) + " : " + originalByte[j]);
		//            }
		//        }
		//        System.out.println("복호화할 바이트 종료");

		// 복호화된 byte 배열 저장할 리스트 선언
		List<byte[]> decByteList = new ArrayList<byte[]>();

		// IV를 저장하라 변수 선언
		byte[] byteIV = DEFAULT_IV.getBytes();

		for(int i=0; i<encByteList.size(); i++){
			byte[] encByte = (byte[])encByteList.get(i);
			byte[] tempDecByte = new byte[16];

			SEED_KISA.SeedDecrypt(encByte, key, tempDecByte);

			// CBC 운영모드
			exclusiveOR(tempDecByte, byteIV);
			byteIV = encByte;

			decByteList.add(tempDecByte);
		}

		return mergeByteList(decByteList);
	}
	
	/**
	 * seed로 암호화된 문자열을 복호화
	 * @param encVal - 암호화 대상 string
	 * @param seedKey - encrypt할때 사용한 key
	 * @return
	 * @throws Exception
	 */
	public static String getSeedDecrypt(String encVal, int[] seedKey) throws Exception{

		// 암호화문 byte 배열 리스트로 변환
		List<byte[]> encByteList = getByteList(encVal, true);

		//        System.out.println("복호화할 바이트 시작");
		//        for(int i=0; i<encByteList.size(); i++){
		//            byte[] originalByte = encByteList.get(i);
		//            for(int j=0; j<originalByte.length; j++){
		//                System.out.println("" + (j+(i*16)) + " : " + originalByte[j]);
		//            }
		//        }
		//        System.out.println("복호화할 바이트 종료");

		// 복호화된 byte 배열 저장할 리스트 선언
		List<byte[]> decByteList = new ArrayList<byte[]>();

		// IV를 저장하라 변수 선언
		byte[] byteIV = DEFAULT_IV.getBytes();

		for(int i=0; i<encByteList.size(); i++){
			byte[] encByte = (byte[])encByteList.get(i);
			byte[] tempDecByte = new byte[16];

			SEED_KISA.SeedDecrypt(encByte, seedKey, tempDecByte);

			// CBC 운영모드
			exclusiveOR(tempDecByte, byteIV);
			byteIV = encByte;

			decByteList.add(tempDecByte);
		}

		return getByteListStr(decByteList, false);
	}

	public static byte [] getSeedEncrypt(byte [] plainData, int [] seedKey) throws Exception {
		// 원문을 byte[] list로 변환
		List<byte[]> byteList = getByteList(plainData);
		// 암호화된 byte[]를 저장할 list
		List<byte[]> encByteList = new ArrayList<byte[]>();

		// IV를 저장하라 변수 선언
		byte[] byteIV = DEFAULT_IV.getBytes();

		for(int i=0; i<byteList.size(); i++){

			byte[] byteVal = (byte[])byteList.get(i);
			byte[] tempEncVal = new byte[16];

			// CBC 운영모드
			exclusiveOR(byteVal, byteIV);

			SEED_KISA.SeedEncrypt(byteVal, seedKey, tempEncVal);

			byteIV = tempEncVal;
			encByteList.add(tempEncVal);
		}
		// list에 담긴 enc문을 byte []로 변환하여 반환
		return mergeByteList(encByteList);
	}
	
	/**
	 * seed로 문자열을 암호화
	 * @param strVal - 암호화 할 string
	 * @param seedKey - encrypt에 사용할 key
	 * @return
	 * @throws Exception
	 */
	public static String getSeedEncrypt(String strVal, int[] seedKey) throws Exception {

		// 원문을 byte[] list로 변환
		List<byte[]> byteList = getByteList(strVal, false);

		//        System.out.println("원본 바이트 시작");
		//        for(int i=0; i<byteList.size(); i++){
		//            byte[] originalByte = byteList.get(i);
		//            for(int j=0; j<originalByte.length; j++){
		//                System.out.println("" + (j+(i*16)) + " : " + originalByte[j]);
		//            }
		//        }
		//        System.out.println("원본 바이트 종료");

		//        System.out.println("암호화된 바이트 시작");
		// 암호화된 byte[]를 저장할 list
		List<byte[]> encByteList = new ArrayList<byte[]>();

		// IV를 저장하라 변수 선언
		byte[] byteIV = DEFAULT_IV.getBytes();

		for(int i=0; i<byteList.size(); i++){

			byte[] byteVal = (byte[])byteList.get(i);
			byte[] tempEncVal = new byte[16];

			// CBC 운영모드
			exclusiveOR(byteVal, byteIV);

			SEED_KISA.SeedEncrypt(byteVal, seedKey, tempEncVal);

			byteIV = tempEncVal;

			//            for(int j=0; j<tempEncVal.length; j++){
			//                System.out.println("" + (j+(i*16)) + " : " + tempEncVal[j]);
			//            }

			encByteList.add(tempEncVal);
		}
		//        System.out.println("암호화된 바이트 종료");

		// list에 담긴 enc문을 str형태로 변환하여 반환
		return getByteListStr(encByteList, true);
	}

	/**
	 * 암/복호화에 사용할 key값을 생성
	 * @param keyStr - 생성할 key값의 string 값
	 * @return
	 * @throws Exception
	 */
	public static int[] getSeedRoundKey(String keyStr) throws Exception{
		int[] seedKey = new int[32];

		SEED_KISA.SeedRoundKey(seedKey, keyStr.getBytes());

		return seedKey;
	}

	/**
	 * CBC 운영모드를 사용하기 위한 XOR 연산 메서드
	 * value1과 value2를 OXR 연산 후 그 결과를 value1에 담는다.
	 * 128bit 확정형
	 * @param value1 - 변수1
	 * @param value2 - 변수2
	 */
	private static void exclusiveOR(byte[] value1, byte[] value2){

		for(int i=0; i<16; i++){
			value1[i] = Integer.valueOf(value1[i] ^ value2[i]).byteValue();
		}

	}

	private static List<byte []> getByteList(byte [] data) throws Exception {
		List<byte[]> byteList = new ArrayList<byte[]>();
		// seed 암호화를 위해선 byte가 무조건 16byte씩 배열이 생성되어야 함으로 빈배열의 공간수를 계산
		int needBlankLength = 0;
		if(data.length % 16 != 0){
			needBlankLength = 16 - (data.length % 16);
		}

		// 위에서 구한 필요한 빈공간의 수를 더하여 다시 배열 생성
		byte[] newTempByte = new byte[data.length + needBlankLength];

		for(int i=0; i<data.length; i++){
			newTempByte[i] = data[i];
		}

		// 이제 newTempByte를 16의 배수의 사이즈를 갖는 배열이 되었다.
		// 16개씩 짤라서 List에 add 한다.
		int inListByteIdx = 0;
		byte[] inListByte = new byte[16];
		for(int i=0; i<newTempByte.length; i++){

			inListByte[inListByteIdx] = newTempByte[i];
			inListByteIdx++;

			if((i + 1) % 16 == 0 && i != 0){
				byteList.add(inListByte);
				inListByte = new byte[16];
				inListByteIdx = 0;
			}
		}

		return byteList;		
	}
	
	/**
	 * seed는 128bit 블록 단위로 암호화 처리한다.
	 * 문자열을 128bit단위로 byte배열을 생성하고, 그 배열을 list형태로 반환한다.
	 * @param nomal
	 * @param isDecode
	 * @return
	 * @throws Exception
	 */
	private static List<byte[]> getByteList(String nomal, boolean isDecode) throws Exception{

		List<byte[]> byteList = new ArrayList<byte[]>();

		byte[] tempByte = null;

		if(isDecode){
//			BASE64Decoder base64Dec = new BASE64Decoder();
//			tempByte = base64Dec.decodeBuffer(nomal);
			tempByte = Base64.decode(nomal);
		}else{
			tempByte = nomal.getBytes(CHARACTER_SET);
		}

		// seed 암호화를 위해선 byte가 무조건 16byte씩 배열이 생성되어야 함으로 빈배열의 공간수를 계산
		int needBlankLength = 0;
		if(tempByte.length % 16 != 0){
			needBlankLength = 16 - (tempByte.length % 16);
		}

		// 위에서 구한 필요한 빈공간의 수를 더하여 다시 배열 생성
		byte[] newTempByte = new byte[tempByte.length + needBlankLength];

		for(int i=0; i<tempByte.length; i++){
			newTempByte[i] = tempByte[i];
		}

		// 이제 newTempByte를 16의 배수의 사이즈를 갖는 배열이 되었다.
		// 16개씩 짤라서 List에 add 한다.
		int inListByteIdx = 0;
		byte[] inListByte = new byte[16];
		for(int i=0; i<newTempByte.length; i++){

			inListByte[inListByteIdx] = newTempByte[i];
			inListByteIdx++;

			if((i + 1) % 16 == 0 && i != 0){
				byteList.add(inListByte);
				inListByte = new byte[16];
				inListByteIdx = 0;
			}
		}

		return byteList;
	}

	private static byte [] mergeByteList(List byteList) {
		// List에 담긴 byte배열이 16으로 고정임으로 * 16
		byte[] listByte = new byte[byteList.size() * 16];

		// List에 담긴 byte배열을 하나의 배열(listByte)에 merge
		for(int i=0; i<byteList.size(); i++){
			byte[] temp = (byte[])byteList.get(i);

			for(int j=0; j<temp.length; j++){
				listByte[j + (16 * i)] = temp[j];
			}
		}

		// blank byte 카운트 세기
		int blankCnt = 0;
		for(int i=listByte.length; i>0; i--){
			if(listByte[i - 1] == 0){
				blankCnt++;
			}else{
				break;
			}
		}

		// blank를 제외한 만큼의 데이터만 추출
		byte[] resultByte = new byte[listByte.length - blankCnt];
		for(int i=0; i<resultByte.length; i++){
			resultByte[i] = listByte[i];
		}
		return resultByte;
	}

	/**
	 * 128bit단위로 쪼개진 byte배열이 담긴 list를 문자열로 변환해준다
	 * @param byteList
	 * @param isEncode
	 * @return
	 * @throws Exception
	 */
	private static String getByteListStr(List byteList, boolean isEncode) throws Exception{

		// List에 담긴 byte배열이 16으로 고정임으로 * 16
		byte[] listByte = new byte[byteList.size() * 16];

		// List에 담긴 byte배열을 하나의 배열(listByte)에 merge
		for(int i=0; i<byteList.size(); i++){
			byte[] temp = (byte[])byteList.get(i);

			for(int j=0; j<temp.length; j++){
				listByte[j + (16 * i)] = temp[j];
			}
		}

		// blank byte 카운트 세기
		int blankCnt = 0;
		for(int i=listByte.length; i>0; i--){
			if(listByte[i - 1] == 0){
				blankCnt++;
			}else{
				break;
			}
		}

		// blank를 제외한 만큼의 데이터만 추출
		byte[] resultByte = new byte[listByte.length - blankCnt];
		for(int i=0; i<resultByte.length; i++){
			resultByte[i] = listByte[i];
		}

		String retStr = null;
		if(isEncode){
//			BASE64Encoder base64Enc = new BASE64Encoder();
//			retStr = base64Enc.encode(resultByte);
			retStr = Base64.encodeToString(resultByte, true);
		}else{
			retStr = new String(resultByte, CHARACTER_SET);
		}
		return retStr;
	}
}
