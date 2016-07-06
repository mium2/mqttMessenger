package com.msp.chat.server.controller;

import com.google.gson.JsonObject;
import java.util.Map;

/**
 /**
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-07
 */
public class PushHttpController{

    public JsonObject getConnectCnt(Map<String,String> reqMap) throws Exception{
        JsonObject rootbject = new JsonObject();
        try {
            rootbject.addProperty("returnData", "");
        }catch(Exception e){
            throw new Exception(e.toString());
        }
        return rootbject;
    }

    public JsonObject getSubscriptionCnt(Map<String,String> reqMap)  throws Exception{
        JsonObject rootbject = new JsonObject();

        return rootbject;
    }

}
