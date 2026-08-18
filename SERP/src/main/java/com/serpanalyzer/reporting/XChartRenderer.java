package com.serpanalyzer.reporting;

import com.serpanalyzer.domain.RankedItem;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.HorizontalBarChart;
import org.knowm.xchart.HorizontalBarChartBuilder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XChartRenderer implements ChartRenderer {
    
    @Override
    public void rankedBars(String title, List<RankedItem> data, Path out) {
        List<RankedItem> safeData = data;
        if (safeData == null || safeData.isEmpty()) {
            safeData = List.of(new RankedItem("No Data Found", 0));
        }
        
        CategoryChart chart = new CategoryChartBuilder()
            .width(800)
            .height(600)
            .title(title)
            .xAxisTitle("Count")
            .yAxisTitle("Label")
            .build();
        
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setPlotContentSize(0.95);
        
        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        
        for (RankedItem item : safeData) {
            labels.add(item.label());
            counts.add(item.count());
        }
        
        chart.addSeries("Count", labels, counts);
        
        try {
            BitmapEncoder.saveBitmap(chart, out.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart", e);
        }
    }

    @Override
    public void rankedBarsHorizontal(String title, List<RankedItem> data, Path out) {
        List<RankedItem> safeData = data;
        if (safeData == null || safeData.isEmpty()) {
            safeData = List.of(new RankedItem("No Subheadings Found", 0));
        }
        
        HorizontalBarChart chart = new HorizontalBarChartBuilder()
            .width(800)
            .height(600)
            .title(title)
            .xAxisTitle("Count")
            .yAxisTitle("Subheading")
            .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setPlotContentSize(0.95);

        List<Integer> counts = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (RankedItem item : safeData) {
            counts.add(item.count());
            labels.add(item.label());
        }

        chart.addSeries("Count", counts, labels);

        try {
            BitmapEncoder.saveBitmap(chart, out.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart", e);
        }
    }
    
    @Override
    public void groupedBars(String title, Map<String, int[]> data, Path out) {
        CategoryChart chart = new CategoryChartBuilder()
            .width(800)
            .height(600)
            .title(title)
            .build();
        
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        
        if (!data.isEmpty()) {
            Map.Entry<String, int[]> firstEntry = data.entrySet().iterator().next();
            int length = firstEntry.getValue().length;
            List<String> categories = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                categories.add("Category " + (i + 1));
            }
            
            for (Map.Entry<String, int[]> entry : data.entrySet()) {
                List<Integer> values = Arrays.stream(entry.getValue())
                    .boxed()
                    .collect(Collectors.toList());
                chart.addSeries(entry.getKey(), categories, values);
            }
        }
        
        try {
            BitmapEncoder.saveBitmap(chart, out.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart", e);
        }
    }
    
    @Override
    public void line(String title, double[] x, double[] y, Path out) {
        XYChart chart = new XYChartBuilder()
            .width(800)
            .height(600)
            .title(title)
            .xAxisTitle("X")
            .yAxisTitle("Y")
            .build();
        
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        
        chart.addSeries("Series", x, y);
        
        try {
            BitmapEncoder.saveBitmap(chart, out.toString(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save chart", e);
        }
    }
}
