package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CrimeReportingAnalyzer implements PaperAnalyzer {
    
    private static final Set<String> STOP_WORDS = Set.of(
        "the", "and", "for", "with", "this", "that", "from", 
        "are", "was", "were", "has", "have", "been", "but",
        "not", "will", "can", "could", "would", "should",
        "may", "might", "must", "shall", "does", "did",
        "into", "through", "during", "before", "after",
        "above", "below", "between", "under", "again",
        "further", "then", "once", "here", "there", "when",
        "where", "why", "how", "all", "both", "each", "few",
        "more", "most", "other", "some", "such", "only",
        "own", "same", "than", "too", "very", "just"
    );
    
    @Override
    public void analyze(Paper paper, Aggregator<String> sink) {
        if (paper.category() != Category.CRIME_REPORTING) {
            return;
        }
        
        String summary = paper.summary();
        if (summary == null || summary.isEmpty()) {
            return;
        }
        
        String cleaned = summary.toLowerCase()
            .replaceAll("[^a-z0-9]", " ");
        
        Set<String> distinctTokens = Arrays.stream(cleaned.split("\\s+"))
            .filter(token -> token.length() > 2)
            .filter(token -> !STOP_WORDS.contains(token))
            .collect(Collectors.toSet());
        
        for (String token : distinctTokens) {
            sink.increment(token);
        }
    }
}
