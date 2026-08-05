package com.serpanalyzer.concurrency;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConcurrencyPrimitivesTest {
    
    @Test
    public void testAggregatorConcurrency() throws InterruptedException {
        log("Starting aggregator concurrency test");
        
        ConcurrentMapAggregator<String> aggregator = new ConcurrentMapAggregator<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                log("Thread " + threadNum + " starting increments");
                for (int j = 0; j < 1000; j++) {
                    aggregator.increment("test-key");
                }
                log("Thread " + threadNum + " finished increments");
                latch.countDown();
            });
        }
        
        latch.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        Map<String, Integer> snapshot = aggregator.snapshot();
        log("Final snapshot: " + snapshot);
        
        assertEquals(10000, snapshot.get("test-key"), 
            "Expected exactly 10000 increments");
    }
    
    @Test
    public void testBufferSentinel() throws InterruptedException {
        log("Starting buffer sentinel test");
        
        BlockingQueueBuffer<String> buffer = new BlockingQueueBuffer<>();
        AtomicInteger itemsRead = new AtomicInteger(0);
        List<String> readItems = new ArrayList<>();
        CountDownLatch producerDone = new CountDownLatch(1);
        CountDownLatch consumerDone = new CountDownLatch(1);
        
        Thread producer = new Thread(() -> {
            log("Producer starting");
            for (int i = 1; i <= 5; i++) {
                String item = "item-" + i;
                log("Producer writing: " + item);
                buffer.write(item);
            }
            log("Producer closing buffer");
            buffer.close();
            producerDone.countDown();
        }, "Producer");
        
        Thread consumer = new Thread(() -> {
            log("Consumer starting");
            while (true) {
                Optional<String> item = buffer.read();
                if (item.isEmpty()) {
                    log("Consumer received empty sentinel, terminating");
                    break;
                }
                log("Consumer read: " + item.get());
                readItems.add(item.get());
                itemsRead.incrementAndGet();
            }
            consumerDone.countDown();
        }, "Consumer");
        
        producer.start();
        consumer.start();
        
        assertTrue(producerDone.await(5, TimeUnit.SECONDS), 
            "Producer should complete within timeout");
        assertTrue(consumerDone.await(5, TimeUnit.SECONDS), 
            "Consumer should complete within timeout");
        
        producer.join();
        consumer.join();
        
        log("Items read: " + itemsRead.get());
        log("Read items: " + readItems);
        
        assertEquals(5, itemsRead.get(), 
            "Expected exactly 5 items to be read");
        assertEquals(5, readItems.size(), 
            "Expected exactly 5 items in the read list");
    }
    
    private static void log(String msg) {
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), msg);
    }
}
