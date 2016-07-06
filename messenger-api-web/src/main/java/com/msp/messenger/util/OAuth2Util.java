package com.msp.messenger.util;

import com.sun.org.apache.xml.internal.security.utils.Base64;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class OAuth2Util {

    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        // 1.9.x 버전 이상
        mapper.setSerializationInclusion(Inclusion.NON_NULL);
        mapper.setSerializationInclusion(Inclusion.NON_EMPTY);
        // 1.8.x 버전 이하.
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES,
                false);
        mapper.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS,
                false);
        mapper.configure(
                DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                true);
    }

    // URL Encoding Utility
    public static String encodeURIComponent(String s) {
        String result;

        try {
            result = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!").replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(").replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = s;
        }

        return result;
    }

    public static String decodeURIComponent(String s) {
        if (s == null) {
            return null;
        }
        String result = null;
        try {
            result = URLDecoder.decode(s, "UTF-8");
        }
        // This exception should never occur.
        catch (UnsupportedEncodingException e) {
            result = s;
        }
        return result;
    }

    public static byte[] hexToBinary(String hex) {
        if (hex == null || hex.length() == 0) {
            return null;
        }

        byte[] ba = new byte[hex.length() / 2];
        for (int i = 0; i < ba.length; i++) {
            ba[i] = (byte) Integer
                    .parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return ba;
    }

    // byte[] to hex
    public static String binaryToHex(byte[] ba) {
        if (ba == null || ba.length == 0) {
            return null;
        }

        StringBuffer sb = new StringBuffer(ba.length * 2);
        String hexNumber;
        for (int x = 0; x < ba.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & ba[x]);

            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }
        return sb.toString();
    }

    public static String getHmacSha256(String str) {
        byte[] binary = null;
        try{
            MessageDigest sh = MessageDigest.getInstance("SHA-256");
            sh.update(str.getBytes("UTF-8"));
            binary = sh.digest();
        }catch(Exception e){
            e.printStackTrace();
        }

        return binaryToHex(binary);
    }

    public static String encodeBase64String(String data) {
        byte[] binary;
        try {
            binary = data.getBytes("UTF-8");
            return Base64.encode(binary);

        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeBase64String(String base64String) {
        try {
            byte[] binary = Base64.decode(base64String);
            return new String(binary, "UTF-8");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    // Access Token과 refresh_token을 랜덤하게 생성함.
    public static String generateToken() {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte ramdomBytes[] = new byte[256];
            secureRandom.nextBytes(ramdomBytes);
            secureRandom.setSeed(ramdomBytes);
//            secureRandom.setSeed(secureRandom.generateSeed(256));  //리눅스 버전 jdk에서 hang이 걸리는 경우가 있음.
            MessageDigest digest = MessageDigest.getInstance("SHA-1"); //MD5,SHA-1,SHA-256
            byte[] dig = digest.digest((secureRandom.nextLong() + "").getBytes());
            return binaryToHex(dig);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static String generateBearerToken(String access_token) {
        return "Bearer " + access_token;
    }

    public static String parseBearerToken(String authHeader) {
        return authHeader.split(" ")[1];
    }

    public static String getJSONFromObject(Object obj) {
        try {
            StringWriter sw = new StringWriter(); // serialize
            mapper.writeValue(sw, obj);
            sw.close();

            return sw.getBuffer().toString();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getObjectFromJSON(String json, Class<T> classOfT) {
        try {
            StringReader sr = new StringReader(json);
            return mapper.readValue(json.getBytes("UTF-8"), classOfT);
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
