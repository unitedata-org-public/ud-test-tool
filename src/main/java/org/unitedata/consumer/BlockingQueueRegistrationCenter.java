package org.unitedata.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * @author: hushi
 * @create: 2019/03/12
 */
public class BlockingQueueRegistrationCenter {

    private static Map<Class, BlockingQueue> queueMap = new HashMap<>();

    public static void register(BlockingQueue blockingQueue, Class clazz) {
        queueMap.put(clazz, blockingQueue);
    }

    public static BlockingQueue getBlockingQueue(Class clazz) {
        return queueMap.get(clazz);
    }
}
