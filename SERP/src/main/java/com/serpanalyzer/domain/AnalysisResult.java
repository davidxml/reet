package com.serpanalyzer.domain;

import java.util.List;

public record AnalysisResult(
    List<RankedItem> task1Features,
    List<RankedItem> task2Subheadings,
    BenchmarkStats benchmark
) {}
