package com.serpanalyzer.concurrency;

import java.util.Map;

public interface Aggregator<K> {
    void increment(K key);
    Map<K, Integer> snapshot();
}
