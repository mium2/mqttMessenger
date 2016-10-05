package com.mium2.messenger.util.client;


import com.msp.chat.core.mqtt.proto.messages.PublishMessage;
import io.netty.channel.ChannelHandlerContext;

public class MessageInfo {
	private ChannelHandlerContext m_ctx;
	private PublishMessage publishMessage;

	public MessageInfo(ChannelHandlerContext _ctx, PublishMessage _publishMessage) {
		m_ctx = _ctx;
		publishMessage = _publishMessage;
	}

	public ChannelHandlerContext getCtx() {
		return m_ctx;
	}

	public PublishMessage getPublishMessage() {
		return publishMessage;
	}
}
