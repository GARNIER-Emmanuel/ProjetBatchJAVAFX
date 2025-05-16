package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
public class MainViewController {

    @FXML
    private Pane leftInclude;

    @FXML
    private ScrollPane centerInclude;

    @FXML
    private VBox rootVBox;

    @FXML
    private void initialize() {
        // Ici tu peux manipuler les composants déjà chargés, par exemple :
        System.out.println("Left pane : " + leftInclude);
        System.out.println("Center scroll pane : " + centerInclude);
        System.out.println("Right tab pane : " + rootVBox);
        
    }
}

