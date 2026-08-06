package com.serpanalyzer.benchmark;

import com.serpanalyzer.analyzer.PaperAnalyzer;
import com.serpanalyzer.concurrency.Aggregator;
import com.serpanalyzer.concurrency.ConcurrentMapAggregator;
import com.serpanalyzer.concurrency.DefaultPipelineExecutor;
import com.serpanalyzer.concurrency.PipelineExecutor;
import com.serpanalyzer.domain.BenchmarkStats;
import com.serpanalyzer.domain.Paper;
import com.serpanalyzer.reporting.ChartRenderer;
import com.serpanalyzer.reporting.CsvReporter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BenchmarkHarness {
    
    private final List<Paper> papers;
    private final PaperAnalyzer analyzer;
    private final Path outputDir;
    
    public BenchmarkHarness(List<Paper> papers, PaperAnalyzer analyzer, Path outputDir) {
        this.papers = papers;
        this.analyzer = analyzer;
        this.outputDir = outputDir;
    }
    
    public BenchmarkStats run() {
        System.out.println("=== Starting Benchmark Harness ===");
        System.out.println("Paper count: " + papers.size());
        
        long sequentialTime = runSequential();
        System.out.println("Sequential execution time: " + sequentialTime + " ms");
        
        long concurrentTime = runConcurrent();
        System.out.println("Concurrent execution time: " + concurrentTime + " ms");
        
        int threadCount = Runtime.getRuntime().availableProcessors();
        BenchmarkStats stats = new BenchmarkStats(sequentialTime, concurrentTime, threadCount);
        
        exportResults(stats);
        
        double speedup = (double) sequentialTime / concurrentTime;
        System.out.println("Speedup: " + String.format("%.2fx", speedup));
        System.out.println("=== Benchmark Complete ===");
        
        return stats;
    }
    
    private long runSequential() {
        Aggregator<String> aggregator = new ConcurrentMapAggregator<>();
        
        long startTime = System.currentTimeMillis();
        
        for (Paper paper : papers) {
            analyzer.analyze(paper, aggregator);
        }
        
        long endTime = System.currentTimeMillis();
        
        return endTime - startTime;
    }
    
    private long runConcurrent() {
        Aggregator<String> aggregator = new ConcurrentMapAggregator<>();
        PipelineExecutor executor = new DefaultPipelineExecutor();
        
        long startTime = System.currentTimeMillis();
        
        for (Paper paper : papers) {
            executor.submit(() -> analyzer.analyze(paper, aggregator));
        }
        
        executor.awaitCompletion();
        
        long endTime = System.currentTimeMillis();
        
        executor.shutdown();
        
        return endTime - startTime;
    }
    
    private void exportResults(BenchmarkStats stats) {
        try {
            CsvReporter.writeBenchmarkStats(outputDir.resolve("benchmark.csv"), stats);
            System.out.println("Benchmark CSV written to: " + outputDir.resolve("benchmark.csv"));
        } catch (Exception e) {
            System.err.println("Failed to write benchmark CSV: " + e.getMessage());
        }
    }
    
    public void exportChart(BenchmarkStats stats, ChartRenderer chartRenderer) {
        try {
            Map<String, int[]> data = new HashMap<>();
            data.put("Sequential", new int[]{(int) stats.sequentialTimeMs()});
            data.put("Concurrent", new int[]{(int) stats.concurrentTimeMs()});
            
            chartRenderer.groupedBars(
                "Performance Benchmark: Sequential vs Concurrent",
                data,
                outputDir.resolve("benchmark.png")
            );
            
            System.out.println("Benchmark chart written to: " + outputDir.resolve("benchmark.png"));
        } catch (Exception e) {
            System.err.println("Failed to write benchmark chart: " + e.getMessage());
        }
    }
}
