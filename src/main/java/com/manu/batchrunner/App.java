package com.manu.batchrunner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1200, 800);

        primaryStage.setTitle("Batch Runner");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMaximized(true);

    }

    public static void main(String[] args) {
        launch(args);
    }

    
}
