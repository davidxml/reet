package com.serpanalyzer.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultPipelineExecutor implements PipelineExecutor {
    private final ExecutorService executor;
    private final AtomicInteger pendingTasks;
    
    public DefaultPipelineExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        this.executor = Executors.newFixedThreadPool(processors);
        this.pendingTasks = new AtomicInteger(0);
    }
    
    @Override
    public void submit(Runnable task) {
        pendingTasks.incrementAndGet();
        executor.submit(() -> {
            try {
                task.run();
            } finally {
                pendingTasks.decrementAndGet();
            }
        });
    }
    
    @Override
    public void awaitCompletion() {
        try {
            // Busy-wait until all tasks complete
            while (pendingTasks.get() > 0) {
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while awaiting completion", e);
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
