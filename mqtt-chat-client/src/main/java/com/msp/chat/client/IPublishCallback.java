package com.msp.chat.client;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2014-05-22
 * Time: 오전 11:06
 * To change this template use File | Settings | File Templates.
 */
public interface IPublishCallback {

    void published(String topic, String message/*, boolean retained*/);
}
