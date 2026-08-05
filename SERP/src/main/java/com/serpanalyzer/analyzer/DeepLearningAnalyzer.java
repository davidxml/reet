package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;

import java.util.HashSet;
import java.util.Set;

/**
 * Task 3.1 &amp; 3.4: Analyzer for Deep Learning papers.
 *
 * <p>Extracts the paper's section sub-headings, normalizes and deduplicates them, then
 * increments the shared {@link Aggregator} once per distinct heading per paper (document
 * frequency).
 */
public class DeepLearningAnalyzer implements PaperAnalyzer {

    private final SubheadingExtractor subheadingExtractor;

    public DeepLearningAnalyzer() {
        this(new SubheadingExtractor());
    }

    DeepLearningAnalyzer(SubheadingExtractor subheadingExtractor) {
        this.subheadingExtractor = subheadingExtractor;
    }

    @Override
    public void analyze(Paper paper, Aggregator<String> sink) {
        if (paper == null || paper.category() != Category.DEEP_LEARNING) {
            return;
        }

        Set<String> rawHeadings = subheadingExtractor.extractSubheadings(paper);
        if (rawHeadings.isEmpty()) {
            return;
        }

        Set<String> distinctNormalized = new HashSet<>();
        for (String heading : rawHeadings) {
            String key = HeadingNormalizer.normalize(heading);
            if (!key.isEmpty()) {
                distinctNormalized.add(key);
            }
        }

        for (String key : distinctNormalized) {
            sink.increment(key);
        }
    }
}
