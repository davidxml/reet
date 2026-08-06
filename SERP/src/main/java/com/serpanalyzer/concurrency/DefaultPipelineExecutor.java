package com.serpanalyzer.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DefaultPipelineExecutor implements PipelineExecutor {
    private final ExecutorService executor;
    private CountDownLatch latch;
    private int taskCount;
    
    public DefaultPipelineExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(processors);
        this.taskCount = 0;
    }
    
    @Override
    public synchronized void submit(Runnable task) {
        if (latch == null) {
            latch = new CountDownLatch(1);
            taskCount = 0;
        }
        taskCount++;
        final CountDownLatch currentLatch = latch;
        executor.submit(() -> {
            try {
                task.run();
            } finally {
                currentLatch.countDown();
            }
        });
    }
    
    @Override
    public synchronized void awaitCompletion() {
        if (latch == null) {
            return;
        }
        CountDownLatch completionLatch = latch;
        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while awaiting completion", e);
        } finally {
            taskCount = 0;
            latch = null;
        }
    }
    
    @Override
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
