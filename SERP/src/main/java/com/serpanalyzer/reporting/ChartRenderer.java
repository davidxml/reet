package com.serpanalyzer.reporting;

import com.serpanalyzer.domain.RankedItem;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface ChartRenderer {
    void rankedBars(String title, List<RankedItem> data, Path out);
    void groupedBars(String title, Map<String, int[]> data, Path out);
    void line(String title, double[] x, double[] y, Path out);
}
