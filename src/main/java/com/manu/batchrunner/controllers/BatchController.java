package com.manu.batchrunner.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import com.manu.batchrunner.utils.LoggerService;

public class BatchController {

    private Preferences prefs = Preferences.userNodeForPackage(BatchController.class);
    private static final String BATCH_FOLDER_PREF_KEY = "batchFolderPath";


    @FXML
    private TextField searchField;

    @FXML
    private ListView<File> batchListView;

    @FXML
    private TextArea batchTextArea;

    private File currentBatchFolder;
    @FXML
    private VBox rootVBox;  // ajoute fx:id="rootVBox" dans le VBox racine du FXML
    private int lastSearchIndex = 0;

    @FXML
    private Button runBatchButton;

    @FXML
    private Button editBatchButton;

    @FXML
    private Button saveBatchButton;

    @FXML
    private Button editInternallyButton;

    @FXML
    private Button createBatchButton;

    @FXML
    private Button changeBatchFolderButton;

    @FXML
    private void runBatchButtonClicked() {
        runSelectedBatch();
    }

    @FXML
    private void editBatchButtonClicked() {
        openSelectedBatchInNotepad();
    }

    @FXML
    private void editInternallyButtonClicked() {
        batchTextArea.setEditable(true);
        batchTextArea.requestFocus();
    }

    @FXML
    private void saveBatchButtonClicked() {
        saveBatchContent();
    }

    @FXML
    private void createBatchButtonClicked() {
        createNewBatch();
    }

    
    @FXML
    public void initialize() {

        String savedPath = prefs.get(BATCH_FOLDER_PREF_KEY, null);
        if (savedPath != null) {
            File savedFolder = new File(savedPath);
            if (savedFolder.exists() && savedFolder.isDirectory()) {
                currentBatchFolder = savedFolder;
                loadBatchesFromFolder();
            }
        } else {
            // Dossier par défaut si pas de prefs enregistrées
            currentBatchFolder = new File("C:\\Users\\manub\\OneDrive\\Desktop\\batches");
            if (currentBatchFolder.exists() && currentBatchFolder.isDirectory()) {
                loadBatchesFromFolder();
            }
        }
        batchTextArea.setEditable(false);
    
        batchListView.setCellFactory(lv -> new ListCell<File>() {
            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    
    
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBatchList(newVal));

    
        // Disable editing by default
        batchTextArea.setEditable(false);

        // Listener pour charger le contenu du batch sélectionné
        batchListView.getSelectionModel().selectedItemProperty().addListener((obs, oldFile, newFile) -> {
            if (newFile != null) {
                displayBatchContent(newFile);
            }
        });

       // écouteur sur la largeur du VBox racine
        rootVBox.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            if (width < THRESHOLD_WIDTH) {
                hideButtonText();
            } else {
                showButtonText();
            }
        });

        searchKeywordField.textProperty().addListener((obs, oldText, newText) -> {
            lastSearchIndex = 0;
        });

    }

    
private final double THRESHOLD_WIDTH = 180; // largeur seuil à ajuster selon besoin


private void hideButtonText() {
    runBatchButton.setText("▶");
    editBatchButton.setText("✏");
    saveBatchButton.setText("💾");
    editInternallyButton .setText("✏");
    createBatchButton.setText("➕");
    changeBatchFolderButton.setText("📂");
}

private void showButtonText() {
    runBatchButton.setText("▶ Lancer le batch");
    editBatchButton.setText("✏ Modifier dans Notepad++");
    saveBatchButton.setText("💾 Enregistrer modifications");
    editInternallyButton .setText("✏ Editer en interne");
    createBatchButton.setText("➕ Créer batch");
    changeBatchFolderButton.setText("📂 Changer dossier batches");
}

    @FXML
    private TextField searchKeywordField;

    @FXML
    private void searchInFlux() {
        String keyword = searchKeywordField.getText();
        String content = batchTextArea.getText();
    
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
            batchTextArea.selectRange(index, index + keyword.length());
            batchTextArea.requestFocus();
    
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

    private void filterBatchList(String filter) {
        if (currentBatchFolder == null) return;

        File[] files = currentBatchFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".bat"));

        if (files == null) return;

        List<File> filtered = List.of(files).stream()
                .filter(f -> f.getName().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());

        batchListView.getItems().setAll(filtered);
    }

    private void chooseBatchFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir un dossier de batches");

        File selectedDir = directoryChooser.showDialog(batchListView.getScene().getWindow());
        if (selectedDir != null && selectedDir.isDirectory()) {
            currentBatchFolder = selectedDir;
            loadBatchesFromFolder();
        }
    }

    private void loadBatchesFromFolder() {
        if (currentBatchFolder == null) return;

        File[] batchFiles = currentBatchFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".bat"));

        batchListView.getItems().clear();

        if (batchFiles != null) {
            batchListView.getItems().addAll(batchFiles);
        }

        batchTextArea.clear();
        batchTextArea.setEditable(false);
    }

    private void displayBatchContent(File file) {
        try {
            String content = Files.readString(file.toPath());
            batchTextArea.setText(content);
            batchTextArea.setEditable(false);
        } catch (IOException e) {
            batchTextArea.setText("Erreur lecture fichier : " + e.getMessage());
        }
    }

    private void saveBatchContent() {
        File selected = batchListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            Files.writeString(selected.toPath(), batchTextArea.getText());
            batchTextArea.setEditable(false);
        } catch (IOException e) {
            showAlert("Erreur sauvegarde", "Impossible d'enregistrer le fichier : " + e.getMessage());
        }
    }

    private void createNewBatch() {
        if (currentBatchFolder == null) {
            showAlert("Dossier non sélectionné", "Veuillez d'abord choisir un dossier de batches.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("nouveau_batch.bat");
        dialog.setTitle("Créer un nouveau batch");
        dialog.setHeaderText("Entrez le nom du nouveau batch (sans extension .bat)");
        dialog.setContentText("Nom du fichier :"+ ".bat");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.toLowerCase().endsWith(".bat")) {
                showAlert("Nom invalide", "Le fichier doit avoir l'extension .bat");
                return;
            }

            File newFile = new File(currentBatchFolder, name);

            if (newFile.exists()) {
                showAlert("Fichier existant", "Un fichier avec ce nom existe déjà.");
                return;
            }

            try {
                if (newFile.createNewFile()) {
                    batchListView.getItems().add(newFile);
                    batchListView.getSelectionModel().select(newFile);
                } else {
                    showAlert("Erreur", "Impossible de créer le fichier.");
                }
            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la création : " + e.getMessage());
            }
        });
    }

    private void openSelectedBatchInNotepad() {
        File selected = batchListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
    
        // Liste des chemins probables
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
    

    private void runSelectedBatch() {
    File selected = batchListView.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    try {
        Process process = new ProcessBuilder("cmd.exe", "/c", selected.getAbsolutePath())
                .redirectErrorStream(true)
                .start();

        // Lire la sortie du batch
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                LoggerService.log("⏳ Exécution de : " + selected.getName());
                Platform.runLater(() -> LoggerService.addToExecutedList(selected.getName()));

                while ((line = reader.readLine()) != null) {
                    LoggerService.log(line);
                }

                process.waitFor();
                LoggerService.log("✅ Fin de l'exécution : " + selected.getName());

            } catch (Exception e) {
                LoggerService.log("❌ Erreur : " + e.getMessage());
            }
        }).start();

    } catch (IOException e) {
        LoggerService.log("❌ Impossible d’exécuter le batch : " + e.getMessage());
    }
}


    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    private void changeBatchFolderButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner un dossier contenant des batchs");
        Window window = batchListView.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(window);

        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            currentBatchFolder = selectedDirectory;
            File[] batchFiles = selectedDirectory.listFiles(f -> f.getName().endsWith(".bat"));
            if (batchFiles != null) {
                batchListView.getItems().setAll(batchFiles);
            }
            // Sauvegarder dans les preferences
            prefs.put(BATCH_FOLDER_PREF_KEY, selectedDirectory.getAbsolutePath());
        }
    }

    
}
