package com.msp.chat.server.netty;

import com.msp.chat.server.commons.utill.BrokerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Y.B.H(mium2) on 16. 7. 18..
 */
public class WebSocketAcceptor {
    private static final Logger LOG = LoggerFactory.getLogger("server");

    static final boolean SSL = BrokerConfig.getProperty("ssl") != null;
//    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
    static final int PORT = BrokerConfig.getIntProperty(BrokerConfig.WEBSOCKET_PORT);

    public void run() throws Exception {
        // Configure SSL.
        final SslContext sslCtx;
        if (SSL) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
        } else {
            sslCtx = null;
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new WebSocketServerInitializer(sslCtx));

            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel ch = b.bind(PORT).sync().channel();

            System.out.println("Open your web browser and navigate to " +
                (SSL? "https" : "http") + "://127.0.0.1:" + PORT + '/');

//            ch.closeFuture().sync();
        } finally {
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
        }
    }

}
