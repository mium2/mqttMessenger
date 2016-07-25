package com.msp.chat.server.netty;

import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.netty.metrics.MessageMetrics;
import com.msp.chat.server.netty.metrics.MessageMetricsCollector;
import com.msp.chat.server.netty.metrics.MessageMetricsHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLEngine;
import java.io.IOException;

/**
 * Created by Y.B.H(mium2) on 16. 4. 5..
 */
public class NettySslMqttAcceptor implements ServerAcceptor {

    private static final Logger LOG = LoggerFactory.getLogger("server");

    EventLoopGroup m_bossGroup;
    EventLoopGroup m_workerGroup;
    MessageMetricsCollector m_metricsCollector = new MessageMetricsCollector();

    public NettySslMqttAcceptor() {
    }

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
                        SSLEngine sslEngine =   SSslContextFactory.getServerContext().createSSLEngine();
                        sslEngine.setUseClientMode(false);
                        pipeline.addLast("ssl", new SslHandler(sslEngine));
                        pipeline.addLast("decoder", new MQTTDecoder());
                        pipeline.addLast("encoder", new MQTTEncoder());
                        pipeline.addLast("metrics", new MessageMetricsHandler(m_metricsCollector));
                        pipeline.addLast("handler", new NettySslMqttHandler());
//                        pipeline.addLast("handler", handler);

                    }
                });
        b.option(ChannelOption.SO_BACKLOG, 128);
        b.option(ChannelOption.SO_REUSEADDR, true);
        b.childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            // Bind and start to accept incoming connections.
            // ChannelFuture f = b.bind(Constants.PORT);
            LOG.info("===============Broker Server binded============");
            Channel f = b.bind("0.0.0.0", BrokerConfig.getIntProperty(BrokerConfig.SSL_PORT)).sync().channel();

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
        LOG.info("Msg read: {}, msg wrote: {}", metrics.messagesRead(), metrics.messagesWrote());
    }
}
