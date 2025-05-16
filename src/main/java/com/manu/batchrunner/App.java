package com.manu.batchrunner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            SplitPane  root = loader.load();

            Scene scene = new Scene(root, 1200, 800);
            // scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm()); // si tu as un CSS

            primaryStage.setTitle("Batch Runner");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue principale.");
        }

        

    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
