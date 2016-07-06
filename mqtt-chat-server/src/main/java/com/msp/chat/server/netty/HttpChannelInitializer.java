package com.msp.chat.server.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오후 2:23
 * To change this template use File | Settings | File Templates.
 */
public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {



    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        // Uncomment the following line if you want HTTPS
        //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
        //engine.setUseClientMode(false);
        //p.addLast("ssl", new SslHandler(engine));

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
