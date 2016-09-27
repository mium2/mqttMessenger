package com.msp.chat.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {



    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));
        p.addLast("idleStateHandler", new IdleStateHandler(5, 5, 0));
        p.addLast("codec", new HttpServerCodec());
        p.addLast("handler", new NettyHttpHandler());

/*
//      파일 업로드일때 사용 하는 Pipeline
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
//        p.addLast("deflater", new HttpContentCompressor());
        p.addLast("handler", new HttpUploadServerHandler());*/
    }
}
