package com.mylinehub.crm.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.springframework.context.ApplicationContext;

public class ThreadDumpUtil {
	
	ApplicationContext applicationContext;
	
	public ThreadDumpUtil(ApplicationContext applicationContext){
		this.applicationContext = applicationContext;
	}

    public void captureThreadDump() {
        Map<Thread, StackTraceElement[]> allThreads = Thread.getAllStackTraces();
        StringBuilder dump = new StringBuilder();
        dump.append("Thread Dump - ").append(System.currentTimeMillis()).append("\n");

        //System.out.println("Appending data to load in new file");
        for (Map.Entry<Thread, StackTraceElement[]> entry : allThreads.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stackTrace = entry.getValue();
            dump.append("\nThread Name: ").append(thread.getName())
                .append(", State: ").append(thread.getState()).append("\n");

            for (StackTraceElement ste : stackTrace) {
                dump.append("\tat ").append(ste).append("\n");
            }
        }

        // Write the thread dump to a file
        //System.out.println("Write the thread dump to a file");
        try (BufferedWriter writer = new BufferedWriter(
            new FileWriter(new File(applicationContext.getEnvironment().getProperty("spring.thread.dump")+"/thread-dump-" + System.currentTimeMillis() + ".txt")))) {
            writer.write(dump.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

