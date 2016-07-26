package com.msp.chat.server.netty;

import com.msp.chat.core.mqtt.Constants;
import com.msp.chat.server.bean.WebSocketMsgBean;
import com.msp.chat.server.worker.WebSocketMsgManager;
import com.msp.chat.server.worker.WebsocketClientIdCtxManager;
import com.msp.chat.server.worker.WebsocketCtxServerHandleManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger("server");
    private static final String WEBSOCKET_PATH = "/webchat";
    private WebSocketServerHandshaker handshaker;


    @Override
    public void messageReceived(ChannelHandlerContext ctx, Object msg) {
        if (!WebsocketCtxServerHandleManager.getInstance().isContainsKey(ctx)) {
            WebsocketCtxServerHandleManager.getInstance().putNettyChannel(ctx);
        }
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send the demo page and favicon.ico
        if ("/".equals(req.uri())) {
//            ByteBuf content = WebSocketServerIndexPage.getContent(getWebSocketLocation(req));
//            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
//            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
//            HttpHeaderUtil.setContentLength(res, content.readableBytes());
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.uri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            if(request.equals("ping")){
                if(LOGGER.isTraceEnabled()) {
                    LOGGER.trace("ping from websocket");
                }
                ctx.channel().write(new TextWebSocketFrame("pong"));
            }else{
                String[] requestArr = request.split("\\|");
                String command = requestArr[0];
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug("###[WebSocketServerHandler handleWebSocketFrame] command:{}",command);
                }
                WebSocketMsgBean webSocketMsgBean = new WebSocketMsgBean(WebsocketCtxServerHandleManager.getInstance().getNettyChannel(ctx),command,requestArr);
                WebSocketMsgManager.getInstance().putWebSocketMsgBean(webSocketMsgBean);
            }
            return;
        }

        if (frame instanceof BinaryWebSocketFrame) {
            try {
                WebSocketFrame webSocketFrame = frame.retain();
                ByteBuf byteBuf = webSocketFrame.content();
                String[] reqArr = new String[5];
                //command 셋팅
                byte[] commandBytes = new byte[10];
                byteBuf.readBytes(commandBytes);
                reqArr[0] = new String(commandBytes,"utf-8");

                //클라이언트 아이디 셋팅
                String clientID = WebsocketCtxServerHandleManager.getInstance().getNettyChannel(ctx).getAttribute(Constants.ATTR_CLIENTID).toString();
                reqArr[1] = clientID;

                // 메세지아이디 셋팅
                byte[] messageIdBytes = new byte[5];
                byteBuf.readBytes(messageIdBytes);
                reqArr[2] = new String(messageIdBytes,"utf-8").trim();

                // 토픽길이 가져오기
                byte[] topicLenBytes = new byte[4];
                byteBuf.readBytes(topicLenBytes);
                int topicLen = Integer.parseInt(new String(topicLenBytes,"utf-8").trim());

                // 토픽셋팅
                byte[] topicBytes = new byte[topicLen];
                byteBuf.readBytes(topicBytes);
                reqArr[3] = new String(topicBytes,"utf-8");

                // 파일명길이 가져오기
                byte[] fileNameLenBytes = new byte[4];
                byteBuf.readBytes(fileNameLenBytes);
                String strFileNameLen = new String(fileNameLenBytes).trim();
                int fileNameLen = Integer.parseInt(strFileNameLen);

                // 파일명 가져오기
                byte[] fileNameBytes = new byte[fileNameLen];
                byteBuf.readBytes(fileNameBytes);

                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);

                WebSocketMsgBean webSocketMsgBean = new WebSocketMsgBean(WebsocketCtxServerHandleManager.getInstance().getNettyChannel(ctx),new String(commandBytes,"utf-8"),reqArr);
                webSocketMsgBean.setFilename(new String(fileNameBytes,"utf-8"));
                webSocketMsgBean.setAttachFile(bytes);
                WebSocketMsgManager.getInstance().putWebSocketMsgBean(webSocketMsgBean);
            }catch (Exception e){
                e.printStackTrace();
            }

//            try {
//                String rev_fileName = System.currentTimeMillis()+"_test.png";
//                String orgFileFullSrc = "/Users/mium2/project/git_repository/mqttMessenger/messenger-api-web/target/messenger-api-web-1.0.0/download_file/"+rev_fileName;
//                FileOutputStream fot = new FileOutputStream(orgFileFullSrc);
//                fot.write(bytes);
//                fot.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            return;
        }
    }

    private static void sendHttpResponse(
            ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        if (WebSocketAcceptor.SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("###[WebSocketServerHandler channelInactive]");
        }
        NettyChannel channel = WebsocketCtxServerHandleManager.getInstance().getNettyChannel(ctx);
        if(ctx!=null) {
            ctx.close(/*false*/);
            // 현재 접속중인 clientID 관리맵에서 삭제처리
            String clientID = (String) channel.getAttribute(Constants.ATTR_CLIENTID);
            if(clientID!=null) {
                boolean doubleLogin = false;
                Object obj = channel.getAttribute(Constants.DOUBLE_LOGIN);
                if(obj!=null){
                    try {
                        doubleLogin = (Boolean)obj;
                        if(LOGGER.isInfoEnabled()) {
                            LOGGER.info("###[WebSocketServerHandler channelInactive] channel.getAttribute(Constants.DOUBLE_LOGIN) : {}",channel.getAttribute(Constants.DOUBLE_LOGIN));
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                // 중복로그인이 아니므로 아이디로 맵핑된 ChannelHandlerContext는 지운다. 중복로그인일 경우는 지우면 안된다. 새로운 ctx롤 덮어씌워 졌기 때문에..
                if(!doubleLogin) {
                    WebsocketClientIdCtxManager.getInstance().removeChannel(clientID);
                }
            }
            WebsocketCtxServerHandleManager.getInstance().removeChannel(ctx);
            if(LOGGER.isTraceEnabled()){
                LOGGER.trace("###[WebSocketServerHandler channelInactive] Connected clientID size():{}", WebsocketClientIdCtxManager.getInstance().getSize());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                if(LOGGER.isTraceEnabled()){
                    LOGGER.trace("###[WebSocketServerHandler userEventTriggered] closed");
                }
                ctx.close();
            }
        }
    }
}
