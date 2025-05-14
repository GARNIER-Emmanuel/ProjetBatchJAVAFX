package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class MainViewController {

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> batchListView;

    @FXML
    private Button runBatchButton;

    @FXML
    private TextArea logTextArea;

    @FXML
    private StackPane centerPane;

    @FXML
    private void initialize() {
        logTextArea.setText("Application initialisée.\n");

        runBatchButton.setOnAction(event -> {
            String selectedBatch = batchListView.getSelectionModel().getSelectedItem();
            if (selectedBatch != null) {
                logTextArea.appendText("Lancement du batch : " + selectedBatch + "\n");
                // Tu peux ajouter ici le vrai lancement de batch plus tard
            } else {
                logTextArea.appendText("Aucun batch sélectionné.\n");
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            logTextArea.appendText("Recherche : " + newVal + "\n");
        });
    }
}
