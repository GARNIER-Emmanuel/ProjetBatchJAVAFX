package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MainViewController {

    @FXML
    private Pane leftInclude;

    @FXML
    private VBox centerInclude;

    @FXML
    private VBox rightInclude;

    private String currentTheme = "default";
    @FXML
    private BorderPane rootBorderPane;

    private void setTheme(String theme) {
        currentTheme = theme;

    // Supprime tous les stylesheets existants
    leftInclude.getStylesheets().clear();
    centerInclude.getStylesheets().clear();
    rightInclude.getStylesheets().clear();
    rootBorderPane.getStylesheets().clear();

     try {
        String basePath = "/styles/" + currentTheme + "/";
        leftInclude.getStylesheets().add(getClass().getResource(basePath + "left_view_style.css").toExternalForm());
        centerInclude.getStylesheets().add(getClass().getResource(basePath + "center_view_style.css").toExternalForm());
        rightInclude.getStylesheets().add(getClass().getResource(basePath + "right_view_style.css").toExternalForm());
        
        // Appliquer un CSS global au root pour styliser la barre de menu, etc.
        rootBorderPane.getStylesheets().add(getClass().getResource(basePath + "main_view_style.css").toExternalForm());
    } catch (NullPointerException e) {
        System.err.println("Fichiers CSS introuvables pour le thème : " + currentTheme);
        e.printStackTrace();
    }
    }

    @FXML
    private void setThemeBlue() {
        setTheme("bleu");
    }

    @FXML
    private void setThemeGreen() {
        setTheme("green");
    }

    @FXML
    private void setThemeDefault() {
        setTheme("default");
    }

    @FXML
    private void setThemePro() {
        setTheme("pro");
    }

    @FXML
    private void initialize() {
        // Optionnel : appliquer un thème par défaut au démarrage
        setThemeDefault();

        
    }
    
}
