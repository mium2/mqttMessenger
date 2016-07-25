package com.msp.chat.server.bean;

import com.msp.chat.server.netty.ServerChannel;

/**
 * Created by Y.B.H(mium2) on 16. 7. 19..
 */
public class WebSocketMsgBean {
    private ServerChannel serverChannel;
    private String command = "";
    private String[] requestArr;


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
}
