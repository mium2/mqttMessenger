package com.msp.chat.server.worker;

import com.msp.chat.server.bean.HttpRequestBean;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.controller.HttpController;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by Y.B.H(mium2) on 16. 8. 3..
 */
public class HttpMsgWorkerThread extends Thread{
    private Logger logger = LoggerFactory.getLogger("server");
    private String ThreadName;
    private final HttpMsgManager httpMsgManager;
    private boolean isRun = true;

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE); //Disk if size exceed
    private HttpPostRequestDecoder decoder;
    private Map<String,String> reqMap = new HashMap<String, String>();
    private HttpRequest request;
    HttpContent httpBodyContent;

    public HttpMsgWorkerThread(String name, HttpMsgManager _httpMsgManager){
        super(name);
        this.ThreadName=getName();
        this.httpMsgManager=_httpMsgManager;
    }
    @Override
    public void run() {
        while(isRun){
            ///push 전송 로직
            HttpRequestBean httpRequestBean = httpMsgManager.takeRequestBean();
            try {
                if (httpRequestBean.getHttpRequest()!=null) {
                    request = httpRequestBean.getHttpRequest();

                    QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
                    Map<String, List<String>> uriAttributes = decoderQuery.parameters();
                    for (Map.Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                        for (String attrVal: attr.getValue()) {
                            reqMap.put(attr.getKey(),attrVal);
                        }
                    }
                    decoder = new HttpPostRequestDecoder(factory, request);
                    if (HttpHeaderUtil.is100ContinueExpected(request)) {
                        httpRequestBean.getCtx().write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE));
                    }
                }

                // Post 방식일때 Http Body에 넘어오는 추가 데이타
                if(httpRequestBean.getHttpContent()!=null){
                    httpBodyContent = httpRequestBean.getHttpContent();
                    if (decoder != null) {
                        try {
                            decoder.offer(httpBodyContent);
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
                            httpRequestBean.getCtx().close();
                            return;
                        }
                    }

                    responseWrite(httpRequestBean.getCtx());
                }
            }catch (Exception e){
                responseFailWrite(httpRequestBean.getCtx(), HttpResponseStatus.INTERNAL_SERVER_ERROR, "SERVER-ERROR".getBytes());
                e.printStackTrace();
            }finally {
                reset();
            }
        }
    }

    private void responseWrite(ChannelHandlerContext ctx){
        HttpController httpController = new HttpController();
        byte[] responseBytes= null;
        boolean isImage = true;
        if(request==null){
            responseFailWrite(ctx, HttpResponseStatus.BAD_REQUEST, "올바르지 않은 요청정보입니다.".getBytes());
            return;
        }

        if(request.uri().startsWith("/images/")) {
            try {
                String fileName = request.uri().substring(request.uri().lastIndexOf("/")+1);
                String ext = fileName.substring(fileName.lastIndexOf(".")+1);
                isImage = httpMsgManager.getCHKIMGSET().contains(ext);
                String saveFileFullSrc = BrokerConfig.getProperty(BrokerConfig.FILE_SAVE_SRC) + "images/";
                if(request.uri().indexOf("thumb/")>0){
                    saveFileFullSrc = saveFileFullSrc+"thumb/";
                }

                File sendFile = new File(saveFileFullSrc+fileName);
                InputStream is = new FileInputStream(sendFile);
                responseBytes = org.apache.commons.io.IOUtils.toByteArray(is);

            } catch (Exception e) {
                responseFailWrite(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage().getBytes());
            }
        }else if(request.uri().startsWith("/etc/")){

        }else if(request.uri().startsWith("/fileUpload")){
            System.out.println("#### httpBodyContent :"+httpBodyContent.toString());


        }else if("/".equals(request.uri())){
            responseBytes = "Hello World~!".getBytes();
        }else{
            responseBytes = "경로가 올바르지 않습니다.".getBytes();
            responseFailWrite(ctx,HttpResponseStatus.NOT_FOUND,responseBytes);
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

        logger.debug("#### reqMap.containsKey(\"contentType\"):"+reqMap.containsKey("contentType"));
        if(reqMap.containsKey("contentType") && httpMsgManager.getCHKIMGSET().contains(reqMap.get("contentType"))) {
            response.headers().set(CONTENT_TYPE, "image/"+reqMap.get("contentType"));
        }else{
            response.headers().set(CONTENT_TYPE, "application/octet-stream");
        }

        response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
        boolean keepAlive = HttpHeaderUtil.isKeepAlive(request);

        if (!keepAlive) {
            ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            response.headers().set("Keep-Alive", "timeout=5, max=100");
            ctx.write(response);
        }

        ctx.flush();
    }

    private void responseFailWrite(ChannelHandlerContext ctx, HttpResponseStatus status, byte[] errMsgBytes){
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.wrappedBuffer(errMsgBytes));
        ctx.flush();
    }

    private void reset() {
        request = null;
        // destroy the decoder to release all resources
        reqMap.clear();
        if(decoder!=null) {
            decoder.destroy();
            decoder = null;
        }
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean isRun) {
        this.isRun = isRun;
    }
}
