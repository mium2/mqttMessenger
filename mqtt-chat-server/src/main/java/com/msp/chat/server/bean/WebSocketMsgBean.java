package com.msp.chat.server.bean;

import com.msp.chat.server.netty.ServerChannel;

/**
 * Created by Y.B.H(mium2) on 16. 7. 19..
 */
public class WebSocketMsgBean {
    private ServerChannel serverChannel;
    private String command = "";
    private String[] requestArr; // requestArr[0]:command, requestArr[1]:connectID, requestArr[2]:messageId, requestArr[3]:topic, requestArr[4]:message
    private String filename;
    private byte[] attachFile;


    public WebSocketMsgBean(ServerChannel _serverChannel,String _command, String[] _requestArr){
        this.serverChannel = _serverChannel;
        this.command = _command;
        this.requestArr = _requestArr;

    }

    public ServerChannel getServerChannel() {
        return serverChannel;
    }

    public void setServerChannel(ServerChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getRequestArr() {
        return requestArr;
    }

    public void setRequestArr(String[] requestArr) {
        this.requestArr = requestArr;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getAttachFile() {
        return attachFile;
    }

    public void setAttachFile(byte[] attachFile) {
        this.attachFile = attachFile;
    }
}
