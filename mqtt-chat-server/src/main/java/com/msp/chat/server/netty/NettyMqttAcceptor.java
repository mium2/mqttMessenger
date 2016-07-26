package com.msp.chat.server.netty;


import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.netty.metrics.MessageMetrics;
import com.msp.chat.server.netty.metrics.MessageMetricsCollector;
import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import com.msp.chat.server.netty.metrics.MessageMetricsHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class NettyMqttAcceptor implements ServerAcceptor {
    
    private static final Logger LOG = LoggerFactory.getLogger(NettyMqttAcceptor.class);

    EventLoopGroup  m_bossGroup;
    EventLoopGroup  m_workerGroup;
    MessageMetricsCollector m_metricsCollector = new MessageMetricsCollector();


    public void initialize() throws IOException {
        m_bossGroup = new NioEventLoopGroup(1);
        m_workerGroup = new NioEventLoopGroup();
        
        final NettyMQTTHandler handler = new NettyMQTTHandler();

        ServerBootstrap b = new ServerBootstrap();
            b.group(m_bossGroup, m_workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline pipeline = ch.pipeline();
                     pipeline.addLast("decoder", new MQTTDecoder());
                     pipeline.addLast("encoder", new MQTTEncoder());
                     pipeline.addLast("metrics", new MessageMetricsHandler(m_metricsCollector));
                     pipeline.addLast("handler", handler);
                 }
             });
             b.option(ChannelOption.SO_BACKLOG, 128);
             b.option(ChannelOption.SO_REUSEADDR, true);
             b.childOption(ChannelOption.SO_KEEPALIVE, true);
        try {    
            // Bind and start to accept incoming connections.
            // ChannelFuture f = b.bind(Constants.PORT);
            Channel f = b.bind("0.0.0.0", BrokerConfig.getIntProperty(BrokerConfig.PORT)).sync().channel();
            LOG.info("===============[NettyMqttAcceptor initialize]Broker Server binded============");
        } catch (InterruptedException ex) {
            LOG.error(null, ex);
        } finally {

        }
    }

    public void close() {
        if (m_workerGroup == null) {
            throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
        }
        if (m_bossGroup == null) {
            throw new IllegalStateException("Invoked close on an Acceptor that wasn't initialized");
        }
        m_workerGroup.shutdownGracefully();
        m_bossGroup.shutdownGracefully();

        MessageMetrics metrics = m_metricsCollector.computeMetrics();
        //LOG.info(String.format("Bytes read: %d, bytes wrote: %d", metrics.readBytes(), metrics.wroteBytes()));
        LOG.info("###[NettyMqttAcceptor close] Msg read: {}, msg wrote: {}", metrics.messagesRead(), metrics.messagesWrote());
    }

}
