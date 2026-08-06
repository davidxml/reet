package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.RankedItem;
import com.serpanalyzer.reporting.ChartRenderer;
import com.serpanalyzer.reporting.CsvReporter;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DeepLearningOutputFormatter {
    
    public static void processAndExport(Aggregator<String> aggregator, ChartRenderer chartRenderer, Path outputDir) {
        Map<String, Integer> snapshot = aggregator.snapshot();
        
        List<RankedItem> rankedItems = snapshot.entrySet().stream()
            .map(entry -> new RankedItem(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingInt(RankedItem::count).reversed())
            .limit(10)
            .toList();
        
        CsvReporter.writeRankedItems(outputDir.resolve("deep-learning-subheadings.csv"), rankedItems);
        
        chartRenderer.rankedBars("Deep Learning: Top Sub-headings", rankedItems, outputDir.resolve("deep-learning-subheadings.png"));
    }
}
