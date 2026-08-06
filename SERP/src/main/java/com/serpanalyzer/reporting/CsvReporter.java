package com.serpanalyzer.reporting;

import com.opencsv.CSVWriter;
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
}
