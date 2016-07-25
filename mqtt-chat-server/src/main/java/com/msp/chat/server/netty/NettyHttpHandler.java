package com.msp.chat.server.netty;

import com.google.gson.JsonObject;
import com.msp.chat.server.controller.PushHttpController;
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
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오후 1:11
 * To change this template use File | Settings | File Templates.
 */
public class NettyHttpHandler extends ChannelHandlerAdapter {

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk if size exceed
    private HttpPostRequestDecoder decoder;
    private Map<String,String> reqMap = new HashMap<String, String>();
    private HttpRequest request;

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof HttpRequest) {
            request = (HttpRequest) msg;

            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Map.Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                for (String attrVal: attr.getValue()) {
                    reqMap.put(attr.getKey(),attrVal);
                }
            }
            decoder = new HttpPostRequestDecoder(factory, request);
            if (HttpHeaderUtil.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
            }
        }

        // Post 방식일때 Http Body에 넘어오는 추가 데이타
        if(msg instanceof HttpContent){
            HttpContent content = (HttpContent) msg;
            if (decoder != null) {
                try {
                    decoder.offer(content);
                    while (decoder.hasNext()) {
                        InterfaceHttpData data = decoder.next();
                        if (data != null) {
                            try {
                                if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                                    Attribute attribute = (Attribute) data;
                                    String value = attribute.getValue();
                                    reqMap.put(attribute.getName(),value);
                                }
                            }catch (IOException e1) {
                                e1.printStackTrace();
                                return;
                            }finally {
                                data.release();
                            }
                        }
                    }
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e1) {
                    e1.printStackTrace();
                    ctx.close();
                    return;
                }
            }

            if (content instanceof LastHttpContent) {  //request를 마지막 까지 다 읽어 드리고 난 후 처리 할것들 구현

            }
        }
    }
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        PushHttpController pushHttpController = new PushHttpController();
        JsonObject rootJsonObj = new JsonObject();
        rootJsonObj.addProperty("resultCode","404");
        rootJsonObj.addProperty("resultMsg","요청하신 URI는 존재 하지 않습니다. 다시 확인 해 주세요~!");
        JsonObject revJsonObj = null;
        try {
            if(request!=null && request.uri().equals("/connectCnt")){
                revJsonObj = pushHttpController.getConnectCnt(reqMap);
            }else if(request.uri()!=null && request.uri().equals("/subscriptionCnt")){
                revJsonObj = pushHttpController.getSubscriptionCnt(reqMap);
            }else if(request.uri()!=null && request.uri().equals("/groupUser")){
//                revJsonObj = pushHttpController.getGroupUserList(reqMap);
            }

            if (revJsonObj != null) {
                rootJsonObj.add("data", revJsonObj);
            }
            rootJsonObj.addProperty("resultCode","200");
            rootJsonObj.addProperty("resultMsg","SUCCESS");
        }catch (Exception e){
            rootJsonObj.addProperty("resultCode","500");
            rootJsonObj.addProperty("resultMsg",e.toString());
        }

        String responseJson = rootJsonObj.toString();
        byte[] resJsonBytes = responseJson.getBytes();

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(resJsonBytes));
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
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("################# exceptionCaught:");
        cause.printStackTrace();
        ctx.close();
    }

    private void reset() {
        request = null;
        // destroy the decoder to release all resources
        decoder.destroy();
        decoder = null;
    }
}
