package com.mylinehub.crm.TaskScheduler;

import org.springframework.context.ApplicationContext;

import com.mylinehub.crm.utils.ThreadDumpUtil;

import lombok.Data;

@Data
public class ThreadDumpRunnable implements Runnable {

    private String jobId;
    private ApplicationContext applicationContext;

    @Override
    public void run() {
        System.out.println("ThreadDumpRunnable started for jobId: " + jobId);
        try {
            ThreadDumpUtil threadDumpUtil = new ThreadDumpUtil(applicationContext);
            threadDumpUtil.captureThreadDump();
            System.out.println("Thread dump captured successfully for jobId: " + jobId);
        } catch (Exception e) {
            System.out.println("Error while writing thread dump file for jobId: " + jobId);
            e.printStackTrace();
        }
        System.out.println("ThreadDumpRunnable completed for jobId: " + jobId);
    }
}
