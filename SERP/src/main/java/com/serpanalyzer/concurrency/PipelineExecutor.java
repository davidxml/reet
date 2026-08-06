package com.serpanalyzer.concurrency;

public interface PipelineExecutor {
    void submit(Runnable task);
    void awaitCompletion();
    void shutdown();
}
