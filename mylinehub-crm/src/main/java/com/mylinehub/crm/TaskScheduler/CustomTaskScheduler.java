package com.mylinehub.crm.TaskScheduler;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class CustomTaskScheduler extends ThreadPoolTaskScheduler {


    private static final long serialVersionUID = 5L;
    private boolean isInitialized = false;
    private final long keepAliveTimeSeconds = 1 * 60 * 60; // 2 hours in seconds
    
    @PostConstruct
    public void initializeScheduler() {
        // Avoid re-initialization if already done
        if (!isInitialized) {
            setPoolSize(2000);  // Adjust the pool size based on your needs
            setThreadNamePrefix("customtaskscheduler-");
            setDaemon(true);

            try {
                
            	initialize();
                // Access the underlying ScheduledThreadPoolExecutor
                ScheduledThreadPoolExecutor executor = getScheduledThreadPoolExecutor();
                // Allow core threads to timeout (so they can die after keepAliveTime)
                executor.allowCoreThreadTimeOut(true);
                // Set keep alive time to 1 hour
                executor.setKeepAliveTime(keepAliveTimeSeconds, TimeUnit.SECONDS);
                
                isInitialized = true;
                System.out.println("CustomTaskScheduler initialized successfully");
            } catch (Exception e) {
                System.out.println("Error initializing CustomTaskScheduler: " + e.getMessage());
                e.printStackTrace();
                // Optionally throw exception or handle failure gracefully
            }
        }
    }

    @PreDestroy
    public void shutdownScheduler() {
        if (isInitialized) {
            try {
                destroy();
                System.out.println("CustomTaskScheduler shutdown gracefully");
            } catch (Exception e) {
                System.out.println("Error shutting down CustomTaskScheduler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
