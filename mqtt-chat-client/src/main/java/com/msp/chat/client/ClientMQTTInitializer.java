package com.msp.chat.client;

import com.msp.chat.core.mqtt.decoder.MQTTDecoder;
import com.msp.chat.core.mqtt.encoder.MQTTEncoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-26
 * Time: 오후 4:25
 * To change this template use File | Settings | File Templates.
 */
public class ClientMQTTInitializer extends ChannelInitializer<SocketChannel> {

    private Client client;

    public ClientMQTTInitializer(Client _client){
        this.client = _client;
    }
    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new MQTTDecoder());
        pipeline.addLast("encoder", new MQTTEncoder());
        pipeline.addLast("handler", new ClientMQTTHandler(client));
    }
}
