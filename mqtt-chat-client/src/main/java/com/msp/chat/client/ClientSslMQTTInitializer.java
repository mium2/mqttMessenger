package com.msp.chat.client;

import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslContext;

/**
 * Created by Y.B.H(mium2) on 16. 4. 5..
 */
public class ClientSslMQTTInitializer  extends ChannelInitializer<SocketChannel> {

    private Client client;
    private final SslContext sslContext;

    public ClientSslMQTTInitializer(Client _client, SslContext sslCtx){
        this.client = _client;
        this.sslContext = sslCtx;
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("ssl", sslContext.newHandler(ch.alloc()));
        pipeline.addLast("decoder", new MQTTDecoder());
        pipeline.addLast("encoder", new MQTTEncoder());
        pipeline.addLast("handler", new ClientMQTTHandler(client));
    }
}
