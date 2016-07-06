package com.msp.chat.server.storage.memory;

import com.msp.chat.server.bean.Subscription;
import com.msp.chat.server.storage.ISubscribeStore;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Y.B.H(mium2) on 15. 8. 28..
 */
public class MemorySubscribeStore implements ISubscribeStore {

    @Override
    public void initPersistentSubscriptions() {

    }

    @Override
    public void addNewSubscription(Subscription newSubscription) {

    }

    @Override
    public void addNewSubscriptionList(List<Subscription> newSubscriptionList) {

    }

    @Override
    public long getAllSubscribeCount() {
        return 0;
    }

    @Override
    public HashSet<String> getSubscribeClientID(String topic) throws Exception{
        return null;
    }

    @Override
    public void removeSubscription(String topic, String clientID) {

    }

    @Override
    public List<Subscription> retrieveAllSubscriptions() {
        return null;
    }

    @Override
    public void close() {

    }
}
