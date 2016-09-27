package com.msp.chat.client;

import com.msp.chat.client.ssl.SSslContextFactory;
import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Created by Y.B.H(mium2) on 16. 4. 5..
 */
public class ClientSslMQTTInitializer  extends ChannelInitializer<SocketChannel> {

    private Client client;
    private final SSLContext sslContext;

    public ClientSslMQTTInitializer(Client _client, SSLContext sslCtx){
        this.client = _client;
        this.sslContext = sslCtx;
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();
        SSLEngine sslEngine =   sslContext.createSSLEngine();

        pipeline.addLast("ssl", new SslHandler(sslEngine));
        pipeline.addLast("decoder", new MQTTDecoder());
        pipeline.addLast("encoder", new MQTTEncoder());
        pipeline.addLast("handler", new ClientMQTTHandler(client));
    }
}
