package com.serpanalyzer.concurrency;

import java.util.Optional;

public interface Buffer<T> {
    void write(T item);
    Optional<T> read();
    void close();
}
