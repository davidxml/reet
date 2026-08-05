package com.serpanalyzer.reporting;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class JavaFxDashboard extends Application {
    private static TextArea logArea;
    
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setWrapText(true);
        
        ImageView chartView1 = new ImageView();
        chartView1.setFitWidth(400);
        chartView1.setFitHeight(300);
        chartView1.setPreserveRatio(true);
        
        ImageView chartView2 = new ImageView();
        chartView2.setFitWidth(400);
        chartView2.setFitHeight(300);
        chartView2.setPreserveRatio(true);
        
        HBox chartBox = new HBox(10);
        chartBox.getChildren().addAll(chartView1, chartView2);
        
        root.setCenter(chartBox);
        root.setBottom(logArea);
        
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("SERP Analyzer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void appendLog(String message) {
        Platform.runLater(() -> {
            if (logArea != null) {
                logArea.appendText(message + "\n");
            }
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
