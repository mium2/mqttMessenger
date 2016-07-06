package com.msp.chat.server.netty;


import com.msp.chat.server.commons.utill.BrokerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */
public class NettyHttpAcceptor{
    private static final Logger LOG = LoggerFactory.getLogger(NettyHttpAcceptor.class);

    //BytesMetricsCollector m_metricsCollector = new BytesMetricsCollector();
    EventLoopGroup m_bossGroup;
    EventLoopGroup  m_workerGroup;
    public void run() throws IOException {
        m_bossGroup = new NioEventLoopGroup(1);
        m_workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(m_bossGroup, m_workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new HttpChannelInitializer());
            // Bind and start to accept incoming connections.
            Channel f = b.bind("0.0.0.0", BrokerConfig.getIntProperty(BrokerConfig.HTTP_PORT)).sync().channel();
            LOG.info("=================Http Server binded===========");
        } catch (InterruptedException ex) {
            LOG.error(null, ex);
        } finally {
//            LOG.info("=================Http Server ShutDown===========");
//            m_bossGroup.shutdownGracefully();
//            m_workerGroup.shutdownGracefully();
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

        //LOG.info(String.format("Bytes read: %d, bytes wrote: %d", metrics.readBytes(), metrics.wroteBytes()));
    }
}
