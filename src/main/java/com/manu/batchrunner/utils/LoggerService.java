package com.manu.batchrunner.utils;

import javafx.scene.control.ListView;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class LoggerService {
    private static TextArea logArea;
    private static ListView<String> executedListView;

    public static void setExecutedList(ListView<String> listView) {
        executedListView = listView;
    }
    
    public static void addToExecutedList(String batchName) {
        if (executedListView != null) {
            Platform.runLater(() -> {
                executedListView.getItems().add(batchName);
            });
        }
    }
    
    public static void setLogArea(TextArea area) {
        logArea = area;
    }

    public static void log(String message) {
        if (logArea != null) {
            Platform.runLater(() -> {
                logArea.appendText(message + "\n");
            });
        } else {
            System.out.println("Logger non initialisé : " + message);
        }
    }
    
}
