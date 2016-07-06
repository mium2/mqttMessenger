package com.msp.chat.core.mqtt.proto.messages.chat;

import com.google.gson.Gson;
import com.msp.chat.core.mqtt.Constants;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 16. 4. 19..
 */
public class MakeChatRoom {
    private byte[] protocol_code = Constants.CHATROOM_MAKE.getBytes();  //length 4byte
    private int appIdLen = 0;
    private String appID;
    private int roomIDLen = 0;
    private String roomID;
    private int aliasLen = 0;
    private String alias;
    private int userIdLen = 0;
    private String userids;

    public MakeChatRoom(String appID, String chatRoomID,String alias,String userids){
        this.appID = appID;
        this.appIdLen = appID.getBytes().length;
        this.roomID = chatRoomID;
        this.roomIDLen = chatRoomID.getBytes().length;
        this.alias = alias;
        this.aliasLen =alias.getBytes().length;
        this.userids = userids;
        this.userIdLen = userids.getBytes().length;
    }

    public MakeChatRoom(ByteBuffer decodeByteBuffer) throws Exception{
        appIdLen = decodeByteBuffer.getInt();
        byte[] appIdByte = new byte[appIdLen];
        decodeByteBuffer.get(appIdByte);
        appID = new String(appIdByte,"utf-8");

        roomIDLen = decodeByteBuffer.getInt();
        byte[] roomIDBytes = new byte[roomIDLen];
        decodeByteBuffer.get(roomIDBytes);
        roomID = new String(roomIDBytes,"utf-8");

        aliasLen = decodeByteBuffer.getInt();
        byte[] aliasBytes = new byte[aliasLen];
        decodeByteBuffer.get(aliasBytes);
        alias = new String(aliasBytes,"utf-8");

        userIdLen = decodeByteBuffer.getInt();
        byte[] userIdsBytes = new byte[userIdLen];
        decodeByteBuffer.get(userIdsBytes);
        userids = new String(userIdsBytes,"utf-8");
    }

    public String getAppID() {
        return appID;
    }

    public byte[] getProtocol_code() {
        return protocol_code;
    }

    public String getRoomID() {
        return roomID;
    }

    public String getAlias() {
        return alias;
    }

    public String getUserids() {
        return userids;
    }

    public byte[] getMakeChatRoomByte(){
        int allocateSize = 4+4+appIdLen+4+roomIDLen+4+aliasLen+4+userIdLen;
        ByteBuffer buf = ByteBuffer.allocate(allocateSize);
        buf.put(protocol_code);
        buf.putInt(appIdLen);
        buf.put(appID.getBytes());
        buf.putInt(roomIDLen);
        buf.put(roomID.getBytes());
        buf.putInt(aliasLen);
        buf.put(alias.getBytes());
        buf.putInt(userIdLen); // ByteUtils.intTobyte(userIdLen)를 이용하여 바이트배열로 바꾸어 넣어도 된다.
        buf.put(userids.getBytes());

        buf.flip(); // 포지션을 0으로 바꾼다.
        return  buf.array();
    }

    @Override
    public String toString() {
        return String.format("appID : %s, roomID : %s, alias : %s, userids : %s",appID,roomID,alias,userids);
    }
}
