package com.msp.chat.server.storage;


import com.msp.chat.server.bean.Subscription;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 14. 5. 7..
 */
public interface ISubscribeStore {


    public void initPersistentSubscriptions();

    public void addNewSubscription(Subscription newSubscription);

    public void addNewSubscriptionList(List<Subscription> newSubscriptionList);

    public long getAllSubscribeCount();

    public HashSet<String> getSubscribeClientID(String topic) throws Exception;

    public void removeSubscription(String topic, String clientID);

    public List<Subscription> retrieveAllSubscriptions();

    public void close();

}
