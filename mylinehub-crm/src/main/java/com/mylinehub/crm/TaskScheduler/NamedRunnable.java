package com.mylinehub.crm.TaskScheduler;


public class NamedRunnable implements Runnable {
    private final Runnable delegate;
    private final String jobId;

    public NamedRunnable(String jobId, Runnable delegate) {
        this.delegate = delegate;
        this.jobId = jobId;
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        String originalName = thread.getName();
        try {
            thread.setName(originalName + "-" + jobId);
            delegate.run();
        } finally {
            thread.setName(originalName);
        }
    }
}

