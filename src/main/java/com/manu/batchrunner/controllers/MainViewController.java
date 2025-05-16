package com.manu.batchrunner.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.SwingFXUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainViewController {

    @FXML
    private Button runBatchButton;

    @FXML
    private Button editBatchButton;         // ouvre dans Notepad++

    @FXML
    private Button editInternallyButton;    // charge dans logTextArea éditable

    @FXML
    private Button saveBatchButton;         // sauvegarde depuis logTextArea

    @FXML
    private TextField searchField;

    @FXML
    private ListView<String> batchListView;

    @FXML
    private TextArea logTextArea;

    @FXML
    private StackPane centerPane;

    @FXML
    private Button createBatchButton; // Conteneur central dans ton FXML

    @FXML
    private StackPane pdfContainer;

    @FXML
    private ScrollPane pdfScrollPane;

    @FXML
    private VBox pdfPagesContainer;

    private ObservableList<String> masterBatchList = FXCollections.observableArrayList();

    private File currentEditingBatchFile;

    private File batchDirectory = new File("batches"); // valeur par défaut
    @FXML
    private Button changeBatchFolderButton;

    @FXML
    private ListView<String> fluxListView;

    @FXML
    private TextArea fluxTextArea;

    @FXML
    private Button editFluxButton, saveFluxButton, createFluxButton, changeFluxFolderButton;

    private File fluxDirectory = new File("flux"); // dossier par défaut
    private File currentEditingFluxFile;
    @FXML
    private TextField searchFluxField;
    
    @FXML
    private ListView<File> fluxListViewXML;
    
    @FXML
    private Button openFluxNotepadButton;
    
    @FXML
    private TextArea fluxEditArea;
    
    private List<File> fluxFiles = new ArrayList<>();
    
    @FXML
    private void initialize() {
        loadFluxFiles();

        changeFluxFolderButton.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionnez le dossier des flux");
            if (fluxDirectory.exists()) {
                directoryChooser.setInitialDirectory(fluxDirectory);
            }
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                fluxDirectory = selectedDirectory;
                logTextArea.appendText("Dossier flux changé en : " + fluxDirectory.getAbsolutePath() + "\n");
                loadFluxFiles(); // Recharge les fichiers XML
            } else {
                logTextArea.appendText("Aucun dossier sélectionné ou dossier invalide.\n");
            }
        });
        
        fluxListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                File file = new File(fluxDirectory, newVal);
                if (file.exists()) {
                    try {
                        currentEditingFluxFile = file;
                        String content = Files.readString(file.toPath());
                        fluxTextArea.setText(content);
                        fluxTextArea.setEditable(false);
                    } catch (IOException ex) {
                        logTextArea.appendText("Erreur lecture fichier flux : " + ex.getMessage() + "\n");
                    }
                }
            }
        });

        try {
            String filePath = "pdf/test.pdf";
            displayPdf(filePath);
            System.out.println("Tentative d'ouverture de : " + filePath);
 // SANS le slash au début
        } catch (IOException e) {
            e.printStackTrace();
        }
        pdfScrollPane.setFitToWidth(true);

        logTextArea.setText("Application initialisée.\n");
        logTextArea.setEditable(false);
        
        pdfScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double deltaY = event.getDeltaY() * 4;  // multiplier par 3 la vitesse
            double newVvalue = pdfScrollPane.getVvalue() - deltaY / pdfPagesContainer.getHeight();
            newVvalue = Math.min(Math.max(newVvalue, 0), 1); // clamp entre 0 et 1
            pdfScrollPane.setVvalue(newVvalue);
            event.consume();  // empêche le scroll par défaut
        });

        FilteredList<String> filteredList = new FilteredList<>(masterBatchList, s -> true);

        // Recherche dynamique
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String lowerCaseFilter = newVal.toLowerCase();

            filteredList.setPredicate(batchName -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                return batchName.toLowerCase().contains(lowerCaseFilter);
            });
            // Optionnel : log recherche
            logTextArea.appendText("Recherche : " + newVal + "\n");
        });

        batchListView.setItems(filteredList);

        runBatchButton.setOnAction(event -> {
            String selectedBatch = batchListView.getSelectionModel().getSelectedItem();
            if (selectedBatch != null) {
                logTextArea.appendText("Lancement du batch : " + selectedBatch + "\n");
                runBatch(selectedBatch);
            } else {
                logTextArea.appendText("Aucun batch sélectionné.\n");
            }
        });

        editBatchButton.setOnAction(event -> {
            String selectedBatch = batchListView.getSelectionModel().getSelectedItem();
            if (selectedBatch != null) {
                currentEditingBatchFile = new File(batchDirectory, selectedBatch);
                if (currentEditingBatchFile.exists()) {
                    try {
                        String notepadPlusPlusPath = "C:\\Program Files\\Notepad++\\notepad++.exe";
                        new ProcessBuilder(notepadPlusPlusPath, currentEditingBatchFile.getAbsolutePath()).start();
                        logTextArea.appendText("Notepad++ lancé pour " + selectedBatch + "\n");
                    } catch (Exception e) {
                        logTextArea.appendText("Erreur lancement Notepad++ : " + e.getMessage() + "\n");
                    }
                } else {
                    logTextArea.appendText("Fichier batch non trouvé.\n");
                }
            } else {
                logTextArea.appendText("Aucun batch sélectionné.\n");
            }
        });

        editInternallyButton.setOnAction(event -> {
            String selectedBatch = batchListView.getSelectionModel().getSelectedItem();
            if (selectedBatch != null) {
                currentEditingBatchFile = new File(batchDirectory, selectedBatch);
                if (currentEditingBatchFile.exists()) {
                    try {
                        String content = Files.readString(currentEditingBatchFile.toPath());
                        logTextArea.setText(content);
                        logTextArea.setEditable(true);
                        logTextArea.appendText("\n--- Edition interne de " + selectedBatch + " ---\n");
                    } catch (Exception e) {
                        logTextArea.setText("Erreur lecture fichier : " + e.getMessage());
                    }
                } else {
                    logTextArea.setText("Fichier batch non trouvé.");
                }
            } else {
                logTextArea.setText("Aucun batch sélectionné.");
            }
        });

        saveBatchButton.setOnAction(event -> {
            if (currentEditingBatchFile != null && currentEditingBatchFile.exists()) {
                try {
                    Files.writeString(currentEditingBatchFile.toPath(), logTextArea.getText());
                    logTextArea.appendText("\nModifications enregistrées.\n");
                    logTextArea.setEditable(false);
                } catch (Exception e) {
                    logTextArea.appendText("\nErreur lors de l'enregistrement : " + e.getMessage() + "\n");
                }
            } else {
                logTextArea.appendText("\nAucun fichier chargé pour l'enregistrement.\n");
            }
        });

        createBatchButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Créer un nouveau batch");
            dialog.setHeaderText("Nom du nouveau fichier batch");
            dialog.setContentText("Entrez le nom (sans .bat) :");
    
            dialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) {
                    logTextArea.appendText("Nom invalide.\n");
                    return;
                }
    
                // Ajoute l'extension si manquante
                if (!name.toLowerCase().endsWith(".bat")) {
                    name += ".bat";
                }
    
                File newBatchFile = new File(batchDirectory, name);
                if (newBatchFile.exists()) {
                    logTextArea.appendText("Le fichier existe déjà.\n");
                    return;
                }
    
                try {
                    boolean created = newBatchFile.createNewFile();
                    if (created) {
                        masterBatchList.add(name);
                        logTextArea.appendText("Nouveau batch créé : " + name + "\n");
    
                        // Optionnel : afficher le contenu vide pour édition
                        currentEditingBatchFile = newBatchFile;
                        logTextArea.setText("");
                        logTextArea.setEditable(true);
                    } else {
                        logTextArea.appendText("Impossible de créer le fichier.\n");
                    }
                } catch (Exception e) {
                    logTextArea.appendText("Erreur lors de la création : " + e.getMessage() + "\n");
                }
            });
        });

        changeBatchFolderButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Sélectionnez le dossier des batches");
            if (batchDirectory.exists()) {
                directoryChooser.setInitialDirectory(batchDirectory);
            }
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                batchDirectory = selectedDirectory;
                logTextArea.appendText("Dossier batchs changé en : " + batchDirectory.getAbsolutePath() + "\n");
                loadBatchFiles();
            } else {
                logTextArea.appendText("Aucun dossier sélectionné ou dossier invalide.\n");
            }
        });
    
        // N'oublie pas d'appeler loadBatchFiles() au démarrage pour charger les batchs du dossier par défaut
        loadBatchFiles();
    
        

        editFluxButton.setOnAction(e -> {
            if (currentEditingFluxFile != null && currentEditingFluxFile.exists()) {
                try {
                    String content = Files.readString(currentEditingFluxFile.toPath());
                    fluxTextArea.setText(content);
                    fluxTextArea.setEditable(true);
                    logTextArea.appendText("Edition du fichier flux : " + currentEditingFluxFile.getName() + "\n");
                } catch (IOException ex) {
                    logTextArea.appendText("Erreur lecture fichier flux : " + ex.getMessage() + "\n");
                }
            }
        });

        saveFluxButton.setOnAction(e -> {
            if (currentEditingFluxFile != null && currentEditingFluxFile.exists()) {
                try {
                    Files.writeString(currentEditingFluxFile.toPath(), fluxTextArea.getText());
                    fluxTextArea.setEditable(false);
                    logTextArea.appendText("Modifications enregistrées pour flux : " + currentEditingFluxFile.getName() + "\n");
                } catch (IOException ex) {
                    logTextArea.appendText("Erreur sauvegarde fichier flux : " + ex.getMessage() + "\n");
                }
            }
        });
    
        createFluxButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Créer un nouveau fichier flux XML");
            dialog.setHeaderText("Nom du nouveau fichier flux (sans .xml)");
            dialog.setContentText("Entrez le nom :");
            dialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) {
                    logTextArea.appendText("Nom invalide.\n");
                    return;
                }
                if (!name.toLowerCase().endsWith(".xml")) {
                    name += ".xml";
                }
                File newFile = new File(fluxDirectory, name);
                if (newFile.exists()) {
                    logTextArea.appendText("Fichier flux existe déjà.\n");
                    return;
                }
                try {
                    boolean created = newFile.createNewFile();
                    if (created) {
                        loadFluxFilesXML();
                        fluxListView.getSelectionModel().select(newFile.getName());
                        fluxListViewXML.getSelectionModel().select(newFile);
                        logTextArea.appendText("Nouveau fichier flux créé : " + name + "\n");
                        currentEditingFluxFile = newFile;
                        fluxTextArea.setText("");
                        fluxTextArea.setEditable(true);
                    } else {
                        logTextArea.appendText("Impossible de créer le fichier flux.\n");
                    }
                } catch (IOException ex) {
                    logTextArea.appendText("Erreur création fichier flux : " + ex.getMessage() + "\n");
                }
            });
        });

    

    // Recherche simple sur le nom du fichier
    searchFluxField.textProperty().addListener((obs, oldVal, newVal) -> {
        List<File> filtered = fluxFiles.stream()
        .filter(f -> newVal == null || newVal.isEmpty() || f.getName().toLowerCase().contains(newVal.toLowerCase()))    
        .collect(Collectors.toList());
        fluxListViewXML.getItems().setAll(filtered);
        
    });

    // Sélection d'un flux pour afficher son contenu dans fluxEditArea
    fluxListViewXML.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        if (newSel != null) {
            try {
                String content = Files.readString(newSel.toPath());
                fluxEditArea.setText(content);
            } catch (IOException e) {
                fluxEditArea.setText("Erreur lecture fichier : " + e.getMessage());
            }
        }
    });

    // Bouton ouvrir avec Notepad++
    openFluxNotepadButton.setOnAction(e -> {
        File selected = fluxListViewXML.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openWithNotepadPlusPlus(selected);
        }
    });




    }


    @FXML
    private void handleChangeFluxFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionnez le dossier des flux");
        if (fluxDirectory.exists()) {
            directoryChooser.setInitialDirectory(fluxDirectory);
        }
        File selectedDirectory = directoryChooser.showDialog(null);
        if (selectedDirectory != null && selectedDirectory.isDirectory()) {
            fluxDirectory = selectedDirectory;
            logTextArea.appendText("Dossier flux changé en : " + fluxDirectory.getAbsolutePath() + "\n");
            loadFluxFiles(); // Recharge les fichiers XML
        } else {
            logTextArea.appendText("Aucun dossier sélectionné ou dossier invalide.\n");
        }
    }
    

    private void loadFluxFiles() {
        if (fluxDirectory.exists() && fluxDirectory.isDirectory()) {
            fluxFiles = Arrays.stream(fluxDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml")))
                              .collect(Collectors.toList());
    
            // Pour fluxListView (noms uniquement)
            List<String> fileNames = fluxFiles.stream().map(File::getName).collect(Collectors.toList());
            fluxListView.setItems(FXCollections.observableArrayList(fileNames));
    
            // Pour fluxListViewXML (objets File complets)
            fluxListViewXML.setItems(FXCollections.observableArrayList(fluxFiles));
        }
    }

    

    private void openWithNotepadPlusPlus(File file) {
        try {
            // Chemin à adapter si besoin
            String notepadPlusPlusPath = "C:\\Program Files\\Notepad++\\notepad++.exe";
            new ProcessBuilder(notepadPlusPlusPath, file.getAbsolutePath()).start();
        } catch (IOException e) {
            e.printStackTrace();
            // Afficher une erreur utilisateur si besoin
        }
    }


    private void loadFluxFilesXML() {
        if (fluxDirectory.exists() && fluxDirectory.isDirectory()) {
            String[] files = fluxDirectory.list((dir, name) -> name.toLowerCase().endsWith(".xml"));
            fluxListView.getItems().clear();
            if (files != null) {
                fluxListView.getItems().addAll(files);
            }
        } else {
            logTextArea.appendText("Le dossier flux XML n'existe pas ou n'est pas un dossier.\n");
        }
    }
    
    private void loadFluxFile(String filename) {
        File file = new File(fluxDirectory, filename);
        if (file.exists()) {
            try {
                currentEditingFluxFile = file;
                String content = Files.readString(file.toPath());
                fluxTextArea.setText(content);
                fluxTextArea.setEditable(false);
            } catch (IOException ex) {
                logTextArea.appendText("Erreur lecture fichier flux : " + ex.getMessage() + "\n");
            }
        }
    }
    



    // Méthode pour exécuter le batch
    private void runBatch(String batchFileName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", new File(batchDirectory, batchFileName).getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String logLine = line;
                    javafx.application.Platform.runLater(() -> logTextArea.appendText(logLine + "\n"));
                }
            }

            int exitCode = process.waitFor();
            javafx.application.Platform.runLater(() -> logTextArea.appendText("Batch finished with exit code: " + exitCode + "\n"));
        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> logTextArea.appendText("Error running batch: " + e.getMessage() + "\n"));
        }
    }
        
    private void displayPdf(String filePath) throws IOException {
        File pdfFile = new File(filePath);
        System.out.println("🔍 Chargement PDF depuis : " + pdfFile.getAbsolutePath());
    
        if (!pdfFile.exists()) {
            System.err.println("⚠️ Le fichier PDF n'existe pas : " + filePath);
            return;
        }
    
        try (PDDocument document = PDDocument.load(pdfFile)) {
            System.out.println("📄 Nombre de pages dans le document : " + document.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(document);
            pdfPagesContainer.getChildren().add(new Label("Test affichage"));

            pdfPagesContainer.getChildren().clear();
    
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                System.out.println("➡️ Rendu page " + (i + 1));
                BufferedImage bim = renderer.renderImageWithDPI(i, 150);
                Image fxImage = SwingFXUtils.toFXImage(bim, null);
    
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);
    
                pdfPagesContainer.getChildren().add(imageView);
            }
            System.out.println("✅ PDF affiché avec succès.");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'affichage du PDF : " + e.getMessage());
            e.printStackTrace();
        }
        pdfPagesContainer.setMinWidth(600);
        pdfPagesContainer.setMinHeight(400);
        pdfPagesContainer.setVisible(true);

    }
    

    private void loadBatchFiles() {
        masterBatchList.clear(); // vide l'ancienne liste
        if (batchDirectory.exists() && batchDirectory.isDirectory()) {
            String[] batchFiles = batchDirectory.list((dir, name) -> name.endsWith(".bat"));
            if (batchFiles != null) {
                masterBatchList.addAll(Arrays.asList(batchFiles));
            }
        } else {
            logTextArea.appendText("Le dossier batchs sélectionné n'existe pas ou n'est pas un dossier.\n");
        }
    }
    
    
    
}
