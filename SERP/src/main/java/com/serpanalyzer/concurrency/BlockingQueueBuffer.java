package com.serpanalyzer.concurrency;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BlockingQueueBuffer<T> implements Buffer<T> {
    private final BlockingQueue<T> queue;
    private final AtomicBoolean closed;
    
    public BlockingQueueBuffer() {
        this.queue = new ArrayBlockingQueue<>(100);
        this.closed = new AtomicBoolean(false);
    }
    
    @Override
    public void write(T item) {
        if (closed.get()) {
            throw new IllegalStateException("Buffer is closed");
        }
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while writing to buffer", e);
        }
    }
    
    @Override
    public Optional<T> read() {
        try {
            while (true) {
                T item = queue.poll(100, TimeUnit.MILLISECONDS);
                if (item != null) {
                    return Optional.of(item);
                }
                if (closed.get() && queue.isEmpty()) {
                    return Optional.empty();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }
    
    @Override
    public void close() {
        closed.set(true);
    }
}
