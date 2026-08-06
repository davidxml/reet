package com.serpanalyzer.concurrency;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ConcurrentMapAggregator<K> implements Aggregator<K> {
    private final ConcurrentHashMap<K, LongAdder> map;
    
    public ConcurrentMapAggregator() {
        this.map = new ConcurrentHashMap<>();
    }
    
    @Override
    public void increment(K key) {
        map.computeIfAbsent(key, k -> new LongAdder()).increment();
    }
    
    @Override
    public Map<K, Integer> snapshot() {
        Map<K, Integer> result = new HashMap<>();
        map.forEach((key, adder) -> result.put(key, adder.intValue()));
        return Map.copyOf(result);
    }
}
