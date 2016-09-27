package com.msp.messenger.util.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2014-04-03.
 */
public class Sha256Util {
    public final static byte[] SALT_KEY = new byte[]{ 66, 65, 121, 80, 115, 82, 83, 88, 79, 119, 87, 103, 98, 69, 109, 77 };	// "BAyPsRSXOwWgbEmM".getBytes();
    public static String getEncrypt(String source, byte[] salt)
    {
        String result = "";
        try
        {
            byte[] a = source.getBytes();
            byte[] bytes = new byte[a.length + salt.length];
            System.arraycopy(a, 0, bytes, 0, a.length);
            System.arraycopy(salt, 0, bytes, a.length, salt.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);

            byte[] byteData = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; ++i)
            {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
            }

            result = sb.toString();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static String getEncrypt(String source)
    {
        String result = "";
        try
        {
            byte[] a = source.getBytes();
            byte[] bytes = new byte[a.length + SALT_KEY.length];
            System.arraycopy(a, 0, bytes, 0, a.length);
            System.arraycopy(SALT_KEY, 0, bytes, a.length, SALT_KEY.length);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);

            byte[] byteData = md.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; ++i)
            {
                sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
            }

            result = sb.toString();
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
