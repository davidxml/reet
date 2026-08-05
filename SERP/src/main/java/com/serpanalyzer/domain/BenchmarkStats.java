package com.serpanalyzer.domain;

public record BenchmarkStats(
    long sequentialTimeMs,
    long concurrentTimeMs,
    int threadCount
) {}
