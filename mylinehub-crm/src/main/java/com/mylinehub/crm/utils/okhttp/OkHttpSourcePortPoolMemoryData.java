package com.mylinehub.crm.utils.okhttp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.ApplicationContext;

/**
 * @author Anand Goel
 * @version 1.0
 */
public class OkHttpSourcePortPoolMemoryData {

    // Non-fair ReentrantLock (as required)
    private static final ReentrantLock lock = new ReentrantLock(false); // false -> non-fair lock

    // Base timeout constant in seconds
    private static final int BASE_TIMEOUT_SECONDS = 2;

    static private BlockingQueue<Integer> portQueue = new LinkedBlockingQueue<>();

    public static void initialize(ApplicationContext applicationContext) {
        //System.out.println("Initialize OkHttpSourcePortPoolService...");
        int startPort = Integer.parseInt(applicationContext.getEnvironment().getProperty("spring.okhttp.startport"));
        int endPort = Integer.parseInt(applicationContext.getEnvironment().getProperty("spring.okhttp.endport"));

        //System.out.println("startPort : "+startPort);
        //System.out.println("endPort : "+endPort);

        for (int port = startPort; port <= endPort; port++) {
            portQueue.add(port);
        }
    }

    public static int workWithSourcePortPoolMemoryData(String action, int port) throws InterruptedException {

        int returnValue = -1;

        // Replaces with non-fair ReentrantLock and timeout-based retry
        while (true) {
            long currentQueueLength = lock.getQueueLength(); // number of threads waiting for the lock
            long timeoutSeconds = BASE_TIMEOUT_SECONDS + currentQueueLength;

            boolean acquired = lock.tryLock(timeoutSeconds, TimeUnit.SECONDS);

            if (acquired) {
                try {
                    switch (action) {
                        case "acquire":
                            returnValue = acquirePort();
                            System.out.println("OkHTTPRequest Acquired port number : " + returnValue);
                            break;
                        case "offer":
                            releasePort(port);
                            System.out.println("OkHTTPRequest has free port number : " + port);
                            break;
                        default:
                            break;
                    }
                    return returnValue;
                } finally {
                    lock.unlock();
                }
            } else {
                // Could not acquire lock, retry with recalculated timeout
                // Optionally log retry behavior (not altering existing logs)
                // System.out.println("Retrying to acquire lock...");
                continue;
            }
        }
    }

    private static int acquirePort() throws InterruptedException {
        return portQueue.take();  // Blocks if no ports available
    }

    private static void releasePort(int port) {
        portQueue.offer(port);
    }

    // Thread-safety review note:
    // No HashMap instances exist in this class.
    // portQueue is a LinkedBlockingQueue, which is already thread-safe.
    // Therefore, no conversion to ConcurrentHashMap is necessary.
}
