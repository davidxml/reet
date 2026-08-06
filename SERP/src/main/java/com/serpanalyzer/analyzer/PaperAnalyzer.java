package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.Paper;

public interface PaperAnalyzer {
    void analyze(Paper paper, Aggregator<String> sink);
}
