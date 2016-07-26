package com.msp.chat.server.controller;

import com.google.gson.JsonObject;
import java.util.Map;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
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
