package com.msp.chat.server.bean;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Created by Y.B.H(mium2) on 16. 8. 3..
 */
public class HttpRequestBean {
    private ChannelHandlerContext ctx;
    private HttpRequest httpRequest;
    private HttpContent httpContent;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public HttpContent getHttpContent() {
        return httpContent;
    }

    public void setHttpContent(HttpContent httpContent) {
        this.httpContent = httpContent;
    }
}
