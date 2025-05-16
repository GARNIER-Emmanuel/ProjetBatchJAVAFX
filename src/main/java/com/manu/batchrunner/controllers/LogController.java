package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class LogController {

    @FXML
    private TextArea logTextArea;

    private static LogController instance;

    @FXML
    private void initialize() {
        instance = this;
    }

    public static void log(String message) {
        if (instance != null) {
            instance.appendText(message);
        } else {
            System.out.println("ZEBILogger not initialized: " + message);
        }
    }

    public void appendText(String message) {
        logTextArea.appendText(message + "\n");
    }

    public void clearLogs() {
        logTextArea.clear();
    }
}
