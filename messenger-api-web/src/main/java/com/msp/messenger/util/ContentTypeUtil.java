package com.msp.messenger.util;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-09-03
 * Time: 오후 3:28
 * To change this template use File | Settings | File Templates.
 */
public class ContentTypeUtil {

    public static String getContentType(String extention){
        String contentType = "";
        if(extention.toLowerCase().equals("jpg") || extention.toLowerCase().equals("jpeg") || extention.toLowerCase().equals("jpe")){
            contentType = "image/jpeg";
        }else if(extention.toLowerCase().equals("tiff") || extention.toLowerCase().equals("tif")){
            contentType = "image/tiff";
        }else if(extention.toLowerCase().equals("png")){
            contentType = "image/png";
        }else if(extention.toLowerCase().equals("gif")){
            contentType = "image/png";
        }else if(extention.toLowerCase().equals("mdb")){
            contentType = "application/msaccess";
        }else if(extention.toLowerCase().equals("doc")){
            contentType = "application/msword";
        }else if(extention.toLowerCase().equals("pdf")){
            contentType = "application/pdf";
        }else if(extention.toLowerCase().equals("xls") || extention.toLowerCase().equals("xlsx")){
            contentType = "application/vnd.ms-excel";
        }else if(extention.toLowerCase().equals("ppt")){
            contentType = "application/vnd.ms-powerpoint";
        }else if(extention.toLowerCase().equals("zip")){
            contentType = "application/zip";
        }else{
            contentType = "application/octet-stream";
        }
        return contentType;
    }
}
