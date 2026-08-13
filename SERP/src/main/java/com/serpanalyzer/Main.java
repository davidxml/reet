package com.serpanalyzer;

import com.serpanalyzer.analyzer.CrimeReportingAnalyzer;
import com.serpanalyzer.analyzer.DeepLearningAnalyzer;
import com.serpanalyzer.analyzer.Task1OutputFormatter;
import com.serpanalyzer.analyzer.Task2OutputFormatter;
import com.serpanalyzer.benchmark.BenchmarkHarness;
import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.concurrency.BlockingQueueBuffer;
import com.serpanalyzer.concurrency.Buffer;
import com.serpanalyzer.concurrency.ConcurrentMapAggregator;
import com.serpanalyzer.concurrency.DefaultPipelineExecutor;
import com.serpanalyzer.concurrency.PipelineExecutor;
import com.serpanalyzer.domain.BenchmarkStats;
import com.serpanalyzer.domain.Category;
import com.serpanalyzer.domain.Paper;
import com.serpanalyzer.fetcher.SemanticScholarFetcher;
import com.serpanalyzer.reporting.ChartRenderer;
import com.serpanalyzer.reporting.JavaFxDashboard;
import com.serpanalyzer.reporting.XChartRenderer;
import javafx.application.Application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    
    private static final Path OUTPUT_DIR = Paths.get("output");
    
    public static void main(String[] args) {
        new Thread(() -> {
            Application.launch(JavaFxDashboard.class, args);
        }).start();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Thread pipelineThread = new Thread(() -> {
            try {
                runFullPipeline();
            } catch (Exception e) {
                JavaFxDashboard.appendLog("[ERROR] Pipeline failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
        pipelineThread.setName("Pipeline-Main");
        pipelineThread.start();
    }
    
    private static void runFullPipeline() throws Exception {
        JavaFxDashboard.appendLog("=== SERP Analyzer Pipeline Starting ===");
        
        Files.createDirectories(OUTPUT_DIR);
        JavaFxDashboard.appendLog("Output directory created: " + OUTPUT_DIR.toAbsolutePath());
        
        SemanticScholarFetcher fetcher = new SemanticScholarFetcher();
        ChartRenderer chartRenderer = new XChartRenderer();
        
        JavaFxDashboard.appendLog("--- Stage 1: Fetching Papers ---");
        
        List<Paper> crimeReportingPapers = new ArrayList<>();
        List<Paper> deepLearningPapers = new ArrayList<>();
        
        Buffer<Paper> crimeBuffer = new BlockingQueueBuffer<>();
        Buffer<Paper> dlBuffer = new BlockingQueueBuffer<>();
        
        Thread crimeFetchThread = new Thread(() -> {
            try {
                JavaFxDashboard.appendLog("Fetching Crime Reporting papers...");
                fetcher.fetch("crime reporting systems", Category.CRIME_REPORTING, crimeBuffer);
                crimeBuffer.close();
                JavaFxDashboard.appendLog("Crime Reporting papers fetched");
            } catch (Exception e) {
                JavaFxDashboard.appendLog("[ERROR] Crime fetch failed: " + e.getMessage());
                crimeBuffer.close(); // Close buffer even on error
            }
        }, "Fetch-Crime");
        
        Thread dlFetchThread = new Thread(() -> {
            try {
                JavaFxDashboard.appendLog("Fetching Deep Learning papers...");
                fetcher.fetch("deep learning models", Category.DEEP_LEARNING, dlBuffer);
                dlBuffer.close();
                JavaFxDashboard.appendLog("Deep Learning papers fetched");
            } catch (Exception e) {
                JavaFxDashboard.appendLog("[ERROR] Deep Learning fetch failed: " + e.getMessage());
                dlBuffer.close(); // Close buffer even on error
            }
        }, "Fetch-DL");
        
        crimeFetchThread.start();
        // Wait for first fetch to complete before starting second to avoid rate limiting
        crimeFetchThread.join();
        
        dlFetchThread.start();
        
        Thread crimeConsumerThread = new Thread(() -> {
            System.out.println("[CONSUMER-CRIME] Started");
            while (true) {
                var paperOpt = crimeBuffer.read();
                if (paperOpt.isEmpty()) {
                    System.out.println("[CONSUMER-CRIME] No more papers, exiting");
                    break;
                }
                crimeReportingPapers.add(paperOpt.get());
                System.out.println("[CONSUMER-CRIME] Added paper, total: " + crimeReportingPapers.size());
            }
        }, "Consumer-Crime");
        
        Thread dlConsumerThread = new Thread(() -> {
            System.out.println("[CONSUMER-DL] Started");
            while (true) {
                var paperOpt = dlBuffer.read();
                if (paperOpt.isEmpty()) {
                    System.out.println("[CONSUMER-DL] No more papers, exiting");
                    break;
                }
                deepLearningPapers.add(paperOpt.get());
                System.out.println("[CONSUMER-DL] Added paper, total: " + deepLearningPapers.size());
            }
        }, "Consumer-DL");
        
        crimeConsumerThread.start();
        dlConsumerThread.start();
        
        crimeFetchThread.join();
        dlFetchThread.join();
        crimeConsumerThread.join();
        dlConsumerThread.join();
        
        JavaFxDashboard.appendLog("Fetched " + crimeReportingPapers.size() + " Crime Reporting papers");
        JavaFxDashboard.appendLog("Fetched " + deepLearningPapers.size() + " Deep Learning papers");
        
        JavaFxDashboard.appendLog("--- Stage 2: Task 1 - Crime Reporting Analysis ---");
        
        Aggregator<String> crimeAggregator = new ConcurrentMapAggregator<>();
        CrimeReportingAnalyzer crimeAnalyzer = new CrimeReportingAnalyzer();
        PipelineExecutor crimeExecutor = new DefaultPipelineExecutor();
        
        for (Paper paper : crimeReportingPapers) {
            crimeExecutor.submit(() -> {
                crimeAnalyzer.analyze(paper, crimeAggregator);
            });
        }
        
        crimeExecutor.awaitCompletion();
        JavaFxDashboard.appendLog("Crime Reporting analysis complete");
        
        Task1OutputFormatter.processAndExport(crimeAggregator, chartRenderer, OUTPUT_DIR);
        JavaFxDashboard.appendLog("Task 1 output files generated");
        
        crimeExecutor.shutdown();
        
        JavaFxDashboard.loadChart(OUTPUT_DIR.resolve("task1-features.png"), "Task 1: Crime Reporting Features");
        
        JavaFxDashboard.appendLog("--- Stage 3: Task 2 - Deep Learning Subheadings Analysis ---");
        
        Aggregator<String> dlAggregator = new ConcurrentMapAggregator<>();
        DeepLearningAnalyzer dlAnalyzer = new DeepLearningAnalyzer();
        PipelineExecutor dlExecutor = new DefaultPipelineExecutor();
        
        for (Paper paper : deepLearningPapers) {
            dlExecutor.submit(() -> {
                dlAnalyzer.analyze(paper, dlAggregator);
            });
        }
        
        dlExecutor.awaitCompletion();
        JavaFxDashboard.appendLog("Deep Learning analysis complete");
        
        Task2OutputFormatter.processAndExport(dlAggregator, chartRenderer, OUTPUT_DIR);
        JavaFxDashboard.appendLog("Task 2 output files generated");
        
        dlExecutor.shutdown();
        
        JavaFxDashboard.loadChart(OUTPUT_DIR.resolve("task2-subheadings.png"), "Task 2: Deep Learning Subheadings");
        
        JavaFxDashboard.appendLog("--- Stage 4: Benchmark Harness ---");
        
        List<Paper> allPapers = new ArrayList<>();
        allPapers.addAll(crimeReportingPapers);
        allPapers.addAll(deepLearningPapers);
        
        BenchmarkHarness benchmarkHarness = new BenchmarkHarness(allPapers, crimeAnalyzer, OUTPUT_DIR);
        BenchmarkStats stats = benchmarkHarness.run();
        
        benchmarkHarness.exportChart(stats, chartRenderer);
        JavaFxDashboard.appendLog("Benchmark complete");
        JavaFxDashboard.appendLog("Sequential: " + stats.sequentialTimeMs() + " ms");
        JavaFxDashboard.appendLog("Concurrent: " + stats.concurrentTimeMs() + " ms");
        JavaFxDashboard.appendLog("Speedup: " + String.format("%.2fx", (double) stats.sequentialTimeMs() / stats.concurrentTimeMs()));
        
        JavaFxDashboard.loadChart(OUTPUT_DIR.resolve("benchmark.png"), "Performance Benchmark");
        
        JavaFxDashboard.appendLog("--- Stage 5: Output Verification ---");
        
        String[] expectedFiles = {
            "task1-features.csv",
            "task1-features.png",
            "task2-subheadings.csv",
            "task2-subheadings.png",
            "benchmark.csv",
            "benchmark.png"
        };
        
        int fileCount = 0;
        for (String filename : expectedFiles) {
            File file = OUTPUT_DIR.resolve(filename).toFile();
            if (file.exists()) {
                fileCount++;
                JavaFxDashboard.appendLog("[OK] " + filename + " (" + file.length() + " bytes)");
            } else {
                JavaFxDashboard.appendLog("[MISSING] " + filename);
            }
        }
        
        JavaFxDashboard.appendLog("=== Pipeline Complete ===");
        JavaFxDashboard.appendLog("Files generated: " + fileCount + "/" + expectedFiles.length);
        JavaFxDashboard.appendLog("Output directory: " + OUTPUT_DIR.toAbsolutePath());
    }
}
