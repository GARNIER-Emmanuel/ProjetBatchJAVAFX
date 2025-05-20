package com.manu.batchrunner.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.manu.batchrunner.utils.LoggerService;

public class FluxController {

    private Preferences prefs = Preferences.userNodeForPackage(FluxController.class);
    private static final String FLUX_FOLDER_PREF_KEY = "fluxFolderPath";

    @FXML
    private ListView<File> fluxListView;

    @FXML
    private TextField searchFluxField;

    @FXML
    private TextArea logTextArea;
    
    @FXML
    private TextArea fluxTextArea;

    @FXML
    private ListView<String> executedBatchesListView;
    @FXML
    private TextField searchKeywordField;

    private File currentFluxFolder;

    @FXML
    private VBox rootVBox; 

    @FXML
    private Button editInternallyButton;

    @FXML
    private Button saveFlux;

    @FXML
    private Button editInNotePad;
    private int lastSearchIndex = 0;

    @FXML
    private Button createFluxButton;

    @FXML
    private Button changeFluxFolderButton;

    @FXML
    private Button searchFluxButton;

    @FXML
    private void editFluxButtonClicked() {
        openSelectedFluxInNotepad();
    }

    @FXML
    private void editInternallyButtonClicked() {
        fluxTextArea.setEditable(true);
        fluxTextArea.requestFocus();
    }

    @FXML
    private void createFluxButtonClicked() {
        createNewFlux();
    }
    private String currentTheme = "default"; // ou "bleu", "green", etc.

    @FXML
    private void saveFluxButtonClicked() {
        if (currentEditedFile == null) {
            showAlert("Erreur", "Aucun fichier sélectionné à sauvegarder.");
            return;
        }

        try {
            String newContent = fluxTextArea.getText();
            Files.writeString(currentEditedFile.toPath(), newContent);
            showAlert("Succès", "Fichier sauvegardé avec succès !");
        } catch (IOException e) {
            showAlert("Erreur", "Impossible de sauvegarder le fichier : " + e.getMessage());
        }
    }


    @FXML
    private void clearLogs() {
        logTextArea.clear();
    }

    private File currentEditedFile;

    @FXML
    private void initialize() {

        
        String cssPath = "/styles/" + currentTheme + "/right_view_style.css";
        URL cssURL = getClass().getResource(cssPath);
        if (cssURL != null) {
            rootVBox.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.err.println("Fichier CSS introuvable : " + cssPath);
        }
    
        // écouteur sur la largeur du VBox racine
        rootVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < THRESHOLD_WIDTH) {
                hideButtonText();
            } else {
                showButtonText();
            }
        });

        fluxListView.setCellFactory(listView -> new javafx.scene.control.ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());  // Affiche uniquement le nom du fichier
                }
            }
        });

        fluxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                displayFluxContent(newFile);
                currentEditedFile = newFile;  // on garde la référence au fichier affiché
                fluxTextArea.setEditable(true); // on autorise l'édition quand un fichier est sélectionné
            }
        });
        
        // Chargement du dossier flux depuis les préférences
        String savedFluxPath = prefs.get(FLUX_FOLDER_PREF_KEY, null);
        if (savedFluxPath != null) {
            File savedFolder = new File(savedFluxPath);
            if (savedFolder.exists() && savedFolder.isDirectory()) {
                currentFluxFolder = savedFolder;
                loadFluxFromFolder();
            } else {
                // dossier par défaut si le chemin sauvegardé n'existe plus
                currentFluxFolder = new File("C:\\Users\\manub\\OneDrive\\Desktop\\flux");
                loadFluxFromFolder();
            }
        } else {
            // pas de prefs, on met le dossier par défaut
            currentFluxFolder = new File("C:\\Users\\manub\\OneDrive\\Desktop\\flux");
            loadFluxFromFolder();
        }
        LoggerService.setLogArea(logTextArea);
        LoggerService.setExecutedList(executedBatchesListView);

        fluxListView.setCellFactory(listView -> new javafx.scene.control.ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getName());
            }
        });


        fluxListView.setEditable(false);

        // Ajout du listener sur le champ de recherche pour filtrer
        searchFluxField.textProperty().addListener((obs, oldText, newText) -> {
            filterFluxList(newText);
        });

        LoggerService.setLogArea(logTextArea);
        LoggerService.setExecutedList(executedBatchesListView);

        fluxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                try {
                    String content = Files.readString(newFile.toPath());
                    fluxTextArea.setText(content);
                } catch (IOException e) {
                    fluxTextArea.setText("Erreur lors de la lecture du fichier.");
                    e.printStackTrace();
                }
            }
        });

         // Disable editing by default
         fluxListView.setEditable(false);


         searchKeywordField.textProperty().addListener((obs, oldText, newText) -> {
            lastSearchIndex = 0;
        });
        
    }

    @FXML
    private void searchInFlux() {
        String keyword = searchKeywordField.getText();
        String content = fluxTextArea.getText();
    
        if (keyword == null || keyword.isEmpty()) {
            return;
        }
    
        // Recherche à partir de la dernière position + 1
        int index = content.indexOf(keyword, lastSearchIndex + 1);
    
        // Si pas trouvé, on repart au début du texte
        if (index == -1) {
            index = content.indexOf(keyword);
        }
    
        if (index >= 0) {
            fluxTextArea.selectRange(index, index + keyword.length());
            fluxTextArea.requestFocus();
    
            lastSearchIndex = index;  // Sauvegarde de la position courante
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Recherche");
            alert.setHeaderText(null);
            alert.setContentText("Aucune occurrence trouvée.");
            alert.showAndWait();
    
            lastSearchIndex = 0; // reset si rien trouvé
        }
    }
    
    private void openSelectedFluxInNotepad() {
        File selected = fluxListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        String[] possiblePaths = {
            "C:\\Program Files\\Notepad++\\notepad++.exe",
            "C:\\Program Files (x86)\\Notepad++\\notepad++.exe"
        };

        for (String path : possiblePaths) {
            File exe = new File(path);
            if (exe.exists()) {
                try {
                    new ProcessBuilder(path, selected.getAbsolutePath()).start();
                    return;
                } catch (IOException e) {
                    showAlert("Erreur", "Impossible d’ouvrir Notepad++ : " + e.getMessage());
                    return;
                }
            }
        }

        showAlert("Notepad++ non trouvé", "Impossible de localiser Notepad++. Vérifie son installation.");
    }

    private void createNewFlux() {
        if (currentFluxFolder == null) {
            showAlert("Dossier non sélectionné", "Veuillez d'abord choisir un dossier de batches.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("nouveau_flux.xml");
        dialog.setTitle("Créer un nouveau flux");
        dialog.setHeaderText("Entrez le nom du nouveau flux (sans extension .xml)");
        dialog.setContentText("Nom du fichier :"+ ".xml");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.toLowerCase().endsWith(".xml")) {
                showAlert("Nom invalide", "Le fichier doit avoir l'extension .xml");
                return;
            }

            File newFile = new File(currentFluxFolder, name);

            if (newFile.exists()) {
                showAlert("Fichier existant", "Un fichier avec ce nom existe déjà.");
                return;
            }

            try {
                if (newFile.createNewFile()) {
                    fluxListView.getItems().add(newFile);
                    fluxListView.getSelectionModel().select(newFile);
                } else {
                    showAlert("Erreur", "Impossible de créer le fichier.");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la création : " + e.getMessage());
            }
        });

        // Listener pour charger le contenu du flux sélectionné
        fluxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                displayFluxContent(newFile);
            }
        });

       
    }
    
    private void displayFluxContent(File file) {
        try {
            String content = Files.readString(file.toPath());
            fluxTextArea.setText(content);
            fluxTextArea.setEditable(false);
        } catch (IOException e) {
            fluxTextArea.setText("Erreur lecture fichier : " + e.getMessage());
        }
    }


     private void showAlert(String title, String message) {
        Alert.AlertType type;
        if (title.equalsIgnoreCase("Erreur")) {
            type = Alert.AlertType.ERROR;
        } else if (title.equalsIgnoreCase("Succès")) {
            type = Alert.AlertType.INFORMATION;
        } else {
            type = Alert.AlertType.NONE;
        }
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadFluxFromFolder() {
        if (currentFluxFolder == null) return;
    
        File[] xmlFiles = currentFluxFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
    
        fluxListView.getItems().clear();
    
        if (xmlFiles != null) {
            fluxListView.getItems().addAll(xmlFiles);
        }
    
        fluxTextArea.clear();
        fluxTextArea.setEditable(false);
    }

    @FXML
    private void openFluxFolderDialog() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner un dossier contenant des fichiers XML");
        File selectedDirectory = directoryChooser.showDialog(fluxListView.getScene().getWindow());

        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            currentFluxFolder = selectedDirectory; // Mettre à jour le dossier courant

            File[] xmlFiles = selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
            if (xmlFiles != null) {
                fluxListView.getItems().clear();
                fluxListView.getItems().addAll(xmlFiles);
            }

            // Sauvegarder dans les préférences
            prefs.put(FLUX_FOLDER_PREF_KEY, selectedDirectory.getAbsolutePath());
        }
    }

    private void filterFluxList(String filter) {
        if (currentFluxFolder == null) return;
    
        File[] xmlFiles = currentFluxFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
    
        if (xmlFiles == null) return;
    
        List<File> filtered = List.of(xmlFiles).stream()
                .filter(f -> f.getName().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    
        fluxListView.getItems().setAll(filtered);
    }

    
    private final double THRESHOLD_WIDTH = 205; // largeur seuil à ajuster selon besoin

    private void hideButtonText() {
        searchFluxButton.setText("🔍");
        editInternallyButton.setText("✏");
        saveFlux.setText("💾");
        editInNotePad .setText("✏");
        createFluxButton.setText("➕");
        changeFluxFolderButton.setText("📂");
    }
    
    private void showButtonText() {
        searchFluxButton.setText("🔍 Rechercher");
        editInternallyButton.setText("✏ Editer en interne");
        saveFlux.setText("💾 Enregistrer modifications");
        editInNotePad .setText("✏ Modifier dans Notepad++");
        createFluxButton.setText("➕ Créer flux");
        changeFluxFolderButton.setText("📂 Changer dossier fluxs");
    }
}
