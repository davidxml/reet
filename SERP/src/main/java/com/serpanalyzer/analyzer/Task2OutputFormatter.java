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

/**
 * Task 3.5: Formats the aggregated sub-headings into output artifacts.
 *
 * <p>After the worker threads have joined, sorts the aggregated sub-headings by frequency in
 * descending order and passes the data to the {@link CsvReporter} (task2-subheadings.csv) and
 * the {@link ChartRenderer} (task2-subheadings.png horizontal ranked bar chart).
 */
public class Task2OutputFormatter {

    public static void processAndExport(Aggregator<String> aggregator, ChartRenderer chartRenderer, Path outputDir) throws IOException {
        Map<String, Integer> subheadingCounts = aggregator.snapshot();

        List<RankedItem> rankedItems = subheadingCounts.entrySet().stream()
                .map(entry -> new RankedItem(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(RankedItem::count).reversed())
                .collect(Collectors.toList());

        CsvReporter.writeRankedItems(outputDir.resolve("task2-subheadings.csv"), rankedItems);

        chartRenderer.rankedBarsHorizontal(
                "Task 2: Deep Learning Paper Subheadings",
                rankedItems,
                outputDir.resolve("task2-subheadings.png"));
    }
}
