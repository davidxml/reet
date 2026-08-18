package com.serpanalyzer.analyzer;

import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.domain.RankedItem;
import com.serpanalyzer.reporting.ChartRenderer;
import com.serpanalyzer.reporting.CsvReporter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Task1OutputFormatter {

    public static void processAndExport(Aggregator<String> aggregator, ChartRenderer chartRenderer, Path outputDir) throws IOException {
        Map<String, Integer> featureCounts = aggregator.snapshot();
        
        List<RankedItem> rankedItems = featureCounts.entrySet().stream()
                .map(entry -> new RankedItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RankedItem::count).reversed())
                .collect(Collectors.toList());
        
        List<RankedItem> topItems = rankedItems.stream()
                .limit(10)
                .collect(Collectors.toList());
        
        CsvReporter.writeRankedItems(outputDir.resolve("task1-features.csv"), topItems);
        
        chartRenderer.rankedBars("Task 1: Crime Reporting Features", topItems, outputDir.resolve("task1-features.png"));
    }
}
