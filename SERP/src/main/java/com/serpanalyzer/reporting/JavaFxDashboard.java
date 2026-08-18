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
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class JavaFxDashboard extends Application {
    private static TextArea logArea;
    private static TilePane chartPane;
    private static Label paperCountLabel;
    private static Label threadCountLabel;
    private static Label speedupLabel;
    private static Label statusLabel;
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #131313;");
        
        // Create main horizontal split: charts on left, logs on right
        HBox mainLayout = new HBox(16);
        
        // LEFT SIDE: Charts area
        VBox chartsSection = new VBox(12);
        HBox.setHgrow(chartsSection, Priority.ALWAYS);
        
        // Header section
        VBox headerSection = new VBox(4);
        
        Label titleLabel = new Label("Execution Overview");
        titleLabel.setStyle(
            "-fx-font-size: 26px; " +
            "-fx-font-weight: 600; " +
            "-fx-text-fill: #ffffff; " +
            "-fx-letter-spacing: -0.02em;"
        );
        
        Label subtitle = new Label("Real-time metrics for concurrent multi-threaded data extraction");
        subtitle.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #c4c7c8;"
        );
        
        headerSection.getChildren().addAll(titleLabel, subtitle);
        
        // Stats cards row
        HBox statsRow = createStatsRow();
        
        // Chart pane - single column on left
        chartPane = new TilePane();
        chartPane.setPrefColumns(1);
        chartPane.setHgap(12);
        chartPane.setVgap(12);
        chartPane.setPadding(new Insets(0));
        chartPane.setAlignment(Pos.TOP_LEFT);
        chartPane.setStyle("-fx-background-color: transparent;");
        
        ScrollPane chartScrollPane = new ScrollPane(chartPane);
        chartScrollPane.setFitToWidth(true);
        chartScrollPane.setFitToHeight(true);
        chartScrollPane.setStyle(
            "-fx-background: #131313; " +
            "-fx-background-color: transparent; " +
            "-fx-border-color: transparent;"
        );
        VBox.setVgrow(chartScrollPane, Priority.ALWAYS);
        
        chartsSection.getChildren().addAll(headerSection, statsRow, chartScrollPane);
        
        // RIGHT SIDE: Logs panel
        VBox logsSection = new VBox(0);
        logsSection.setPrefWidth(450);
        logsSection.setMinWidth(400);
        logsSection.setMaxWidth(500);
        logsSection.setStyle(
            "-fx-background-color: #080808; " +
            "-fx-border-color: rgba(255, 255, 255, 0.15); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        
        // Console header
        HBox consoleHeader = new HBox(16);
        consoleHeader.setPadding(new Insets(12, 16, 12, 16));
        consoleHeader.setAlignment(Pos.CENTER_LEFT);
        consoleHeader.setStyle(
            "-fx-background-color: rgba(0, 0, 0, 0.4); " +
            "-fx-border-color: rgba(255, 255, 255, 0.05); " +
            "-fx-border-width: 0 0 1 0;"
        );
        
        Label consoleLabel = new Label("LIVE THREAD LOG");
        consoleLabel.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: #c4c7c8; " +
            "-fx-letter-spacing: 0.05em;"
        );
        
        consoleHeader.getChildren().add(consoleLabel);
        
        // Log area with green terminal text
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-control-inner-background: #080808; " +
            "-fx-text-fill: #00ff41; " +
            "-fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace; " +
            "-fx-font-size: 11px; " +
            "-fx-background-color: #080808; " +
            "-fx-border-color: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-padding: 12; " +
            "-fx-highlight-fill: rgba(0, 255, 65, 0.2); " +
            "-fx-highlight-text-fill: #00ff41;"
        );
        VBox.setVgrow(logArea, Priority.ALWAYS);
        
        logsSection.getChildren().addAll(consoleHeader, logArea);
        
        // Add both sections to main layout
        mainLayout.getChildren().addAll(chartsSection, logsSection);
        
        root.setCenter(mainLayout);
        
        Scene scene = new Scene(root, 1600, 950);
        
        // Try to load external CSS if available
        try {
            String cssPath = getClass().getResource("/dashboard.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            // CSS file not found, using inline styles only
        }
        
        primaryStage.setTitle("SERP Analyzer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        
        appendLog("[SYSTEM] Dashboard initialized. Waiting for analysis results...");
    }
    
    private HBox createStatsRow() {
        HBox statsRow = new HBox(12);
        statsRow.setAlignment(Pos.CENTER_LEFT);
        
        // Stat Card 1: Total Papers
        VBox paperCard = createStatCard("TOTAL PAPERS", "0", "papers fetched");
        paperCountLabel = (Label) ((VBox) paperCard.getChildren().get(1)).getChildren().get(0);
        
        // Stat Card 2: Active Threads
        VBox threadCard = createStatCard("THREADS", "0", "concurrent");
        threadCountLabel = (Label) ((VBox) threadCard.getChildren().get(1)).getChildren().get(0);
        
        // Stat Card 3: Speedup Ratio
        VBox speedupCard = createStatCard("SPEEDUP", "0x", "vs sequential");
        speedupLabel = (Label) ((VBox) speedupCard.getChildren().get(1)).getChildren().get(0);
        
        // Stat Card 4: Status
        VBox statusCard = createStatCard("STATUS", "IDLE", "awaiting start");
        statusLabel = (Label) ((VBox) statusCard.getChildren().get(1)).getChildren().get(0);
        
        statsRow.getChildren().addAll(paperCard, threadCard, speedupCard, statusCard);
        
        return statsRow;
    }
    
    private VBox createStatCard(String label, String value, String subtext) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(12));
        card.setPrefWidth(170);
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.03); " +
            "-fx-border-color: rgba(255, 255, 255, 0.08); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        
        Label cardLabel = new Label(label);
        cardLabel.setStyle(
            "-fx-font-size: 9px; " +
            "-fx-font-weight: 700; " +
            "-fx-text-fill: #8e9192; " +
            "-fx-letter-spacing: 0.08em;"
        );
        
        VBox valueBox = new VBox(2);
        
        Label mainValue = new Label(value);
        mainValue.setStyle(
            "-fx-font-size: 20px; " +
            "-fx-font-weight: 600; " +
            "-fx-text-fill: #ffffff;"
        );
        
        Label subValue = new Label(subtext);
        subValue.setStyle(
            "-fx-font-size: 10px; " +
            "-fx-text-fill: #8e9192;"
        );
        
        valueBox.getChildren().addAll(mainValue, subValue);
        card.getChildren().addAll(cardLabel, valueBox);
        
        return card;
    }
    
    public static void updateStats(int paperCount, int threadCount, double speedup, String status) {
        Platform.runLater(() -> {
            if (paperCountLabel != null) {
                paperCountLabel.setText(String.valueOf(paperCount));
            }
            if (threadCountLabel != null) {
                threadCountLabel.setText(String.valueOf(threadCount));
            }
            if (speedupLabel != null) {
                speedupLabel.setText(String.format("%.1fx", speedup));
            }
            if (statusLabel != null) {
                statusLabel.setText(status.toUpperCase());
            }
        });
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
                        imageView.setFitWidth(700);
                        imageView.setFitHeight(400);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                        
                        Label label = new Label(chartTitle);
                        label.setStyle(
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 500; " +
                            "-fx-text-fill: #ffffff; " +
                            "-fx-padding: 0 0 8 0;"
                        );
                        label.setAlignment(Pos.CENTER_LEFT);
                        label.setMaxWidth(Double.MAX_VALUE);
                        
                        VBox chartBox = new VBox(10);
                        chartBox.getChildren().addAll(label, imageView);
                        chartBox.setAlignment(Pos.TOP_LEFT);
                        chartBox.setPrefWidth(750);
                        chartBox.setStyle(
                            "-fx-background-color: rgba(255, 255, 255, 0.04); " +
                            "-fx-border-color: rgba(255, 255, 255, 0.1); " +
                            "-fx-border-width: 1; " +
                            "-fx-border-radius: 12; " +
                            "-fx-background-radius: 12; " +
                            "-fx-padding: 20;"
                        );
                        
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
