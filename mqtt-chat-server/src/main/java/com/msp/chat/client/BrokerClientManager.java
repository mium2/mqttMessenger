package com.msp.chat.client;

import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import com.msp.chat.core.mqtt.proto.messages.AbstractMessage;
import com.msp.chat.core.mqtt.proto.messages.ConnectMessage;
import com.msp.chat.core.mqtt.proto.messages.PingReqMessage;
import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import com.msp.chat.server.bean.ServerInfoBean;
import com.msp.chat.server.bean.events.ValueEvent;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.storage.BrokerConInfoStore;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by Y.B.H(mium2) on 16. 6. 27..
 */
public class BrokerClientManager {
    private String THIS_SERVERID = "";
    private String BROKER_CLIENTID = "";
    private boolean running = true;
    private final static Logger logger = LoggerFactory.getLogger("server");
    private Map<String,ServerInfoBean> brokerInfoMap;
    private HashSet<String> reconUpnsIpSet = new HashSet<String>();
    private Map<String,ChannelFuture> channelFutureMap = new HashMap<String, ChannelFuture>();

    private static final int KEEPALIVE_SECS = 60;
    public static ApplicationContext ctx = null;
    private static BrokerClientManager instance = null;

    private static int MAX_MESSAGE_QUEUE_SIZE = 1000000; //한꺼번에 100만껀 까지 발송정보 담을 수있음.
    private ValueEvent[] valueEventQueue;

    public static BrokerClientManager getInstance(){
        if(instance==null){
            instance = new BrokerClientManager();
        }
        return instance;
    }
    public void init(){
        this.THIS_SERVERID = BrokerConfig.getProperty(BrokerConfig.SERVER_ID);
        this.BROKER_CLIENTID =BrokerConfig.SYSTEM_BROKER_CLIENT_PRIFIX+THIS_SERVERID;

        brokerInfoMap = BrokerConInfoStore.getInstance().getServerInfBeanMap();
        Set<Map.Entry<String,ServerInfoBean>> brokerInfoSet = brokerInfoMap.entrySet();
        for(Map.Entry<String,ServerInfoBean> brokerInfoEntry : brokerInfoSet){
            final String brokerID = brokerInfoEntry.getKey();
            logger.info("###[BrokerClientManager init] Broker server ID : "+brokerID);
            if(brokerID.equals(THIS_SERVERID)){
                continue;
            }
            ServerInfoBean serverInfoBean = brokerInfoEntry.getValue();
            logger.info("####[BrokerClientManager init] Broker connect IP/Port  : "+serverInfoBean.getIP()+"   "+serverInfoBean.getPORT());
            NioEventLoopGroup group = new NioEventLoopGroup(5);
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new MQTTDecoder());
                            pipeline.addLast("encoder", new MQTTEncoder());
                            pipeline.addLast("handler", new ClientMQTTHandler(brokerID));
                        }
                    });
                // Start the client.
                ChannelFuture f = bootstrap.connect(serverInfoBean.getIP(), Integer.parseInt(serverInfoBean.getPORT()));
                f.awaitUninterruptibly();
                assert f.isDone();
                if(f.isCancelled()){
                    // 사용자의 의해 Connection 취소
                } else if(!f.isSuccess()){
                    // 실패 처리
                    logger.error("###[BrokerClientManager init]" +brokerID+ "("+serverInfoBean.getIP()+") brokerID not Start~~!");
                } else {
                    // connection 성공
                    logger.debug("###[BrokerClientManager init]:"+serverInfoBean.getIP()+ " connect clientID:"+BROKER_CLIENTID+ "  status : " +f.channel().isActive());
                    ConnectMessage connMsg = new ConnectMessage();
                    connMsg.setKeepAlive(KEEPALIVE_SECS);
                    connMsg.setClientID(BROKER_CLIENTID);
                    connMsg.setCleanSession(true);
                    f.channel().writeAndFlush(connMsg);
                    channelFutureMap.put(brokerID, f);
                }

            }catch (Exception e){
                logger.info("###[BrokerClientManager init]" +brokerID+ " brokerID not Start~~!");
            }
        }

        try {
            while(running) {
                Thread.sleep(1000 * KEEPALIVE_SECS);
                sendPing();
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        releaseExternalResources();
    }

    private void sendPing() {
        brokerInfoMap = BrokerConInfoStore.getInstance().getServerInfBeanMap();
        Set<Map.Entry<String,ServerInfoBean>> brokerInfoSet = brokerInfoMap.entrySet();
        for(Map.Entry<String,ServerInfoBean> brokerInfoEntry : brokerInfoSet){
            String brokerID = brokerInfoEntry.getKey();
            if(THIS_SERVERID.equals(brokerID)){
                continue;
            }
            if(logger.isTraceEnabled()) {
                logger.trace("###[BrokerClientManager sendPing] brokerID:{} ", brokerID);
            }
            if(channelFutureMap.containsKey(brokerID)){
                Channel channel = channelFutureMap.get(brokerID).channel();
                if(channel.isActive()) {
                    PingReqMessage ping = new PingReqMessage();
                    channel.writeAndFlush(ping);
                    logger.debug("###[BrokerClientManager sendPing] send ping");
                }else{
                    retryConnect(brokerID);
                }
            }else{
                retryConnect(brokerID);
            }
        }
    }

    public void retryConnect(final String brokerid){
        if(brokerInfoMap.containsKey(brokerid)){
            ServerInfoBean serverInfoBean = brokerInfoMap.get(brokerid);
            NioEventLoopGroup group = new NioEventLoopGroup(5);
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new MQTTDecoder());
                            pipeline.addLast("encoder", new MQTTEncoder());
                            pipeline.addLast("handler", new ClientMQTTHandler(brokerid));
                        }
                    });
                // Start the client.
                ChannelFuture f = bootstrap.connect(serverInfoBean.getIP(), Integer.parseInt(serverInfoBean.getPORT()));
                f.awaitUninterruptibly();
                assert f.isDone();
                if(f.isCancelled()){
                    // 사용자의 의해 Connection 취소
                } else if(!f.isSuccess()){
                    // 실패 처리
                } else {
                    // connection 성공
                    logger.debug("###[BrokerClientManager retryConnect] : Connect IP :"+serverInfoBean.getIP()+ " connect clientID:"+BROKER_CLIENTID+ "  Status : " +f.channel().isActive());
                    ConnectMessage connMsg = new ConnectMessage();
                    connMsg.setKeepAlive(KEEPALIVE_SECS);
                    connMsg.setClientID(BROKER_CLIENTID);
                    connMsg.setCleanSession(true);
                    f.channel().writeAndFlush(connMsg);
                    channelFutureMap.put(brokerid, f);
                }

            }catch (Exception e){
                logger.info("###[BrokerClientManager retryConnect]" + brokerid + " brokerID not Start~~!");
            }
        }
    }

    public void sendSysMessage(String brokerid,ByteBuffer message,String topic,int messageID) throws Exception{
        if(logger.isTraceEnabled()){
            logger.trace("###[BrokerClientManager sendSysMessage args3] borkerid :"+brokerid);
        }
        if(channelFutureMap.containsKey(brokerid)) {
            Channel channel = channelFutureMap.get(brokerid).channel();
            if(logger.isTraceEnabled()){
                logger.trace("###[BrokerClientManager sendSysMessage] channel isActive :"+channel.isActive());
            }
            if(channel.isActive()) {
                PublishMessage pubMessage = new PublishMessage();
                pubMessage.setRetainFlag(false);
                pubMessage.setTopicName(topic);
                pubMessage.setQos(AbstractMessage.QOSType.LEAST_ONE);
                pubMessage.setPayload(message.duplicate());
                pubMessage.setMessageID(messageID);
                channel.writeAndFlush(pubMessage);
            }else {
                throw new Exception("###[BrokerClientManager sendSysMessage] ERROR "+brokerid+" is Not Active!");
            }
        }else{
            throw new Exception("###[BrokerClientManager sendSysMessage] ERROR "+brokerid+" not Connected~~!");
        }
    }

    public void sendSysMessage(String brokerid, PublishMessage publishMessage) throws Exception{
        if(logger.isTraceEnabled()){
            logger.trace("###[BrokerClientManager sendSysMessage args2] borkerid :"+brokerid);
        }
        if(channelFutureMap.containsKey(brokerid)) {
            Channel channel = channelFutureMap.get(brokerid).channel();
            if(logger.isTraceEnabled()){
                logger.trace("###[BrokerClientManager sendSysMessage] channel isActive :"+channel.isActive());
            }
            if(channel.isActive()) {
                channel.writeAndFlush(publishMessage);
            }else {
                throw new Exception("###[BrokerClientManager sendSysMessage] ERROR "+brokerid+" is Not Active!");
            }
        }else{
            throw new Exception("###[BrokerClientManager sendSysMessage] ERROR "+brokerid+" not Connected~~!");
        }
    }

    public void releaseExternalResources(){
        this.running = false;
        if(channelFutureMap.size()>0){
            Set<Map.Entry<String,ChannelFuture>> channelFutureSet = channelFutureMap.entrySet();
            for(Map.Entry<String,ChannelFuture> channelFutureEntry : channelFutureSet){
                ChannelFuture channelFuture = channelFutureEntry.getValue();
                if(channelFuture.channel().isActive()){
                    channelFuture.channel().close();
                }
            }
            channelFutureMap = new HashMap<String, ChannelFuture>();
        }
    }

}
