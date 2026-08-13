package com.serpanalyzer.reporting;

import com.opencsv.CSVWriter;
import com.serpanalyzer.domain.BenchmarkStats;
import com.serpanalyzer.domain.RankedItem;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CsvReporter {
    
    public static void writeRankedItems(Path outPath, List<RankedItem> items) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outPath.toFile()))) {
            String[] header = {"Label", "Count"};
            writer.writeNext(header);
            
            for (RankedItem item : items) {
                String[] row = {item.label(), String.valueOf(item.count())};
                writer.writeNext(row);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }
    
    public static void writeBenchmarkStats(Path outPath, BenchmarkStats stats) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(outPath.toFile()))) {
            String[] header = {"Metric", "Value"};
            writer.writeNext(header);
            
            writer.writeNext(new String[]{"Sequential Time (ms)", String.valueOf(stats.sequentialTimeMs())});
            writer.writeNext(new String[]{"Concurrent Time (ms)", String.valueOf(stats.concurrentTimeMs())});
            writer.writeNext(new String[]{"Thread Count", String.valueOf(stats.threadCount())});
            
            String speedupStr;
            if (stats.concurrentTimeMs() == 0) {
                if (stats.sequentialTimeMs() == 0) {
                    speedupStr = "1.00x"; // Both zero, no speedup
                } else {
                    speedupStr = "∞"; // Concurrent was instant
                }
            } else {
                speedupStr = String.format("%.2fx", (double) stats.sequentialTimeMs() / stats.concurrentTimeMs());
            }
            writer.writeNext(new String[]{"Speedup", speedupStr});
        } catch (IOException e) {
            throw new RuntimeException("Failed to write benchmark CSV file", e);
        }
    }
}
