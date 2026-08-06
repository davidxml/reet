package com.serpanalyzer.reporting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class JavaFxDashboard extends Application {
    private static TextArea logArea;
    private static TilePane chartPane;
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        
        Label titleLabel = new Label("SERP Analyzer Dashboard");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 10, 0));
        
        chartPane = new TilePane();
        chartPane.setPrefColumns(2);
        chartPane.setHgap(15);
        chartPane.setVgap(15);
        chartPane.setPadding(new Insets(10));
        chartPane.setAlignment(Pos.CENTER);
        
        ScrollPane chartScrollPane = new ScrollPane(chartPane);
        chartScrollPane.setFitToWidth(true);
        chartScrollPane.setFitToHeight(true);
        chartScrollPane.setStyle("-fx-background-color: #f4f4f4;");
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        Label logLabel = new Label("Live Thread Logs:");
        logLabel.setStyle("-fx-font-weight: bold;");
        logLabel.setPadding(new Insets(10, 0, 5, 0));
        
        VBox logBox = new VBox(5);
        logBox.getChildren().addAll(logLabel, logArea);
        logBox.setPadding(new Insets(10, 0, 0, 0));
        
        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titleLabel, chartScrollPane);
        
        root.setTop(topBox);
        root.setCenter(logBox);
        
        Scene scene = new Scene(root, 1280, 900);
        primaryStage.setTitle("SERP Analyzer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        
        appendLog("[SYSTEM] Dashboard initialized. Waiting for analysis results...");
    }
    
    public static void appendLog(String message) {
        Platform.runLater(() -> {
            if (logArea != null) {
                String timestamp = String.format("[%tT]", System.currentTimeMillis());
                String threadName = Thread.currentThread().getName();
                logArea.appendText(timestamp + " [" + threadName + "] " + message + "\n");
                logArea.setScrollTop(Double.MAX_VALUE);
            }
        });
    }
    
    public static void loadChart(Path chartPath, String chartTitle) {
        Platform.runLater(() -> {
            if (chartPane != null) {
                File chartFile = chartPath.toFile();
                if (chartFile.exists()) {
                    try {
                        Image image = new Image(chartFile.toURI().toString());
                        ImageView imageView = new ImageView(image);
                        imageView.setFitWidth(580);
                        imageView.setFitHeight(400);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        
                        Label label = new Label(chartTitle);
                        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                        label.setAlignment(Pos.CENTER);
                        label.setMaxWidth(Double.MAX_VALUE);
                        
                        VBox chartBox = new VBox(5);
                        chartBox.getChildren().addAll(label, imageView);
                        chartBox.setAlignment(Pos.CENTER);
                        chartBox.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px; -fx-padding: 10px;");
                        
                        chartPane.getChildren().add(chartBox);
                        appendLog("[SYSTEM] Chart loaded: " + chartTitle);
                    } catch (Exception e) {
                        appendLog("[ERROR] Failed to load chart: " + chartTitle + " - " + e.getMessage());
                    }
                } else {
                    appendLog("[WARNING] Chart file not found: " + chartPath);
                }
            }
        });
    }
    
    public static void clearCharts() {
        Platform.runLater(() -> {
            if (chartPane != null) {
                chartPane.getChildren().clear();
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
