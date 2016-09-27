package com.msp.chat.server.netty;

import com.google.gson.JsonObject;
import com.msp.chat.server.bean.HttpRequestBean;
import com.msp.chat.server.controller.HttpController;
import com.msp.chat.server.worker.HttpMsgManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class NettyHttpHandler extends ChannelHandlerAdapter {

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk if size exceed

    private HttpRequestBean httpRequestBean = null;
    private Logger logger = LoggerFactory.getLogger("server");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        logger.debug("## channelRead :"+msg.toString());
        if(httpRequestBean==null) {
            httpRequestBean = new HttpRequestBean();
        }
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            httpRequestBean.setHttpRequest(request);
        }
        // Post 방식일때 Http Body에 넘어오는 추가 데이타
        if(msg instanceof HttpContent){
            HttpContent content = (HttpContent) msg;
            httpRequestBean.setHttpContent(content);
            if (content instanceof LastHttpContent) {  //request를 마지막 까지 다 읽어 드리고 난 후 처리 할것들 구현

            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        httpRequestBean.setCtx(ctx);
        HttpMsgManager.getInstance().putRequestBean(httpRequestBean);
        httpRequestBean = null;

        /*
        logger.debug("## channelReadComplete " + request.uri());
        HttpController httpController = new HttpController();
        byte[] responseBytes = null;

        if(request!=null && request.uri().startsWith("/download_file")){
            responseBytes = "TEST OK".getBytes();

        }else{
            responseBytes = "TEST FAIL".getBytes();
        }
        try {
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseBytes));
        // new getMethod
        HttpHeaders headers = request.headers();
        if (!headers.isEmpty()) {
            for (Map.Entry<CharSequence, CharSequence> h: headers) {
                CharSequence key = h.getKey();
                CharSequence value = h.getValue();
                response.headers().set(key, value);
            }
        }
        // new getMethod
        Set<Cookie> cookies;
        String value = request.headers().getAndConvert(COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.decode(value);
        }
        response.headers().set(CONTENT_TYPE, "text/plain");
        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

        boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);


        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.write(response);
        }

        ctx.flush();
        reset();
        */
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
