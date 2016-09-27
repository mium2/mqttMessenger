package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: mium2(Yoo Byung Hee)
 * Date: 2015-07-17
 * Time: 오후 12:34
 * To change this template use File | Settings | File Templates.
 */
public class OfflineCache {
    private final Logger LOGGER = LoggerFactory.getLogger("server");
    private static OfflineCache INSTANCE;

    private int offlinecount = 10;
    private Map<String,Object> cacheMap = new HashMap<String,Object>();

    public static OfflineCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OfflineCache();
        }
        return INSTANCE;
    }

    public void remove(final String name) throws Exception {

        synchronized (this) {
            cacheMap.remove(name);
        }
        System.out.println("OfflineMessage DEL topic:" + name);
    }
}
