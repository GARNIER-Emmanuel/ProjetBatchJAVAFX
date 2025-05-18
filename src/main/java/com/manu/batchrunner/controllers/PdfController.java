package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
// Removed redundant import
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
// Removed redundant import
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.prefs.Preferences;
import java.io.File;
import java.io.IOException;
import com.manu.batchrunner.utils.LoggerService;
import javafx.stage.DirectoryChooser;
import java.util.Arrays;
import java.util.stream.Collectors;

import javafx.scene.Node;
import org.apache.pdfbox.text.PDFTextStripper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.prefs.Preferences;

public class PdfController implements Initializable {

    private File pdfFolder;
    private List<File> pdfFiles;   // liste des fichiers PDF triés par date décroissante
    private int currentPdfIndex = 0; // index du PDF actuellement affiché
    private static final String PREF_KEY_LAST_PDF_FOLDER = "lastPdfFolder";

    private VBox activePdfPagesContainer;  // conteneur des pages affichées (pdfPagesContainer1 ou 2)

    @FXML
    private ImageView pdfImageView;

    @FXML
    private HBox pdfContainerHBox;

    @FXML
    private VBox pdfPagesContainer;

    @FXML
    private Button zoomOutButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private VBox rootCenterView; 
    @FXML
    private VBox pdfPagesContainer1;

    @FXML
    private VBox pdfPagesContainer2;

    @FXML
    private ScrollPane pdfScrollPane1;
    @FXML
    private Button previousPdfButton;
    
    @FXML
    private Button nextPdfButton;
    
    @FXML
    private ScrollPane pdfScrollPane2;
    @FXML
    private Button toggleImageOverlayButton;
    @FXML
    private TextArea comparisonResultTextArea;

    private boolean overlayMode = false;
    private File pdfFile1; // À assigner avec le PDF 1
    private File pdfFile2; // À assigner avec le PDF 2
    @FXML
    private Button changePdfFolderButton;
    private File currentPdfFolder;
    
    private double scale1 = 1.0;
    private double scale2 = 1.0;

    private final double scaleStep = 0.1;
    private final double minScale = 0.3;
    private final double maxScale = 3.0;

    private List<ImageView> pageImageViews = new ArrayList<>();

    private final double baseWidth = 600; 
        
    
    public void afficherPdf(String cheminPdf) {
        File file = new File(cheminPdf);
    if (!file.exists()) {
        LoggerService.log("Fichier PDF non trouvé : " + cheminPdf);
        return;
    }
    this.pdfFile1 = file;  // <--- Ici on met à jour pdfFile1

        try (PDDocument document = PDDocument.load(new File(cheminPdf))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pdfPagesContainer1.getChildren().clear();
            pageImageViews.clear();
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                Image fxImage = SwingFXUtils.toFXImage(bim, null);
                ImageView iv = new ImageView(fxImage);
            
                iv.setFitWidth(baseWidth);

                iv.setPreserveRatio(true);
                
                pdfPagesContainer1.getChildren().add(iv);
                pageImageViews.add(iv);   // <-- Ajoute ici
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }


    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    
        
        loadLastPdfFolderIfExists();
        pdfScrollPane2.setVisible(false);
        pdfScrollPane2.setManaged(false);
        pdfPagesContainer1.getChildren().clear();

        try {
            // Rendre la première page de chaque PDF en image
            Image img1 = renderPdfPageAsImage(pdfFile1, 0);
            Image img2 = renderPdfPageAsImage(pdfFile2, 0);

            // Nettoyer les conteneurs avant d'afficher
            pdfPagesContainer1.getChildren().clear();
            pdfPagesContainer2.getChildren().clear();

            // Créer ImageView pour chaque image
            ImageView iv1 = new ImageView(img1);
            ImageView iv2 = new ImageView(img2);

            // Ajuster taille et garder ratio
            iv1.setPreserveRatio(true);
            iv2.setPreserveRatio(true);
            iv1.setFitWidth(400); // Ajuste la largeur selon ta fenêtre
            iv2.setFitWidth(400);

            // Ajouter les images aux conteneurs
            pdfPagesContainer1.getChildren().add(iv1);
            pdfPagesContainer2.getChildren().add(iv2);

        } catch (IOException e) {
            e.printStackTrace();
        }
       
        
        final double scrollSpeedFactor = 4.0; // Ajuste ce facteur pour accélérer ou ralentir

        activePdfPagesContainer = pdfPagesContainer1;
        if (pdfScrollPane1 != null) {
            pdfScrollPane1.addEventFilter(ScrollEvent.SCROLL, event -> {
                double deltaY = event.getDeltaY();
                double newValue = pdfScrollPane1.getVvalue() - scrollSpeedFactor * deltaY / pdfPagesContainer1.getHeight();
                pdfScrollPane1.setVvalue(clamp(newValue, 0, 1));
            });
        }
    
        if (pdfScrollPane2 != null) {
            pdfScrollPane2.addEventFilter(ScrollEvent.SCROLL, event -> {
                double deltaY = event.getDeltaY();
                double newValue = pdfScrollPane2.getVvalue() - scrollSpeedFactor * deltaY / pdfPagesContainer2.getHeight();
                pdfScrollPane2.setVvalue(clamp(newValue, 0, 1));
            });
        }

        currentPdfFolder = new File("C:\\Users\\manub\\OneDrive\\Desktop\\flux");
        if (currentPdfFolder.exists() && currentPdfFolder.isDirectory()) {
            loadPdfFromFolder();
        }

        String lastPath = loadLastPdfFolder();
        if (lastPath != null) {
            File folder = new File(lastPath);
            if (folder.exists() && folder.isDirectory()) {
                // récupère ton premier PDF par exemple :
                File[] pdfs = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                if (pdfs != null && pdfs.length > 0) {
                    pdfFile1 = pdfs[0];
                    loadPdfToContainer(pdfFile1, pdfPagesContainer1);
                }
            }
        }
       

        rootCenterView.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
                event.consume();
            }
        });
        
        pdfScrollPane1.setOnMouseClicked(e -> {
            activePdfPagesContainer = pdfPagesContainer1;
            System.out.println("PDF 1 actif");
        });
        
        pdfScrollPane2.setOnMouseClicked(e -> {
            activePdfPagesContainer = pdfPagesContainer2;
            System.out.println("PDF 2 actif");
        });

        final Preferences prefs = Preferences.userNodeForPackage(PdfController.class);

        rootCenterView.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                double deltaY = event.getDeltaY();
                if (deltaY > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
                event.consume();
            }
        });

        if (pdfFile1 != null) loadPdfToContainer(pdfFile1, pdfPagesContainer1);
        if (pdfFile2 != null) loadPdfToContainer(pdfFile2, pdfPagesContainer2);
        LoggerService.log("pdfFile1 = " + (pdfFile1 != null ? pdfFile1.getAbsolutePath() : "null"));

        pdfScrollPane2.setVisible(false);
        pdfScrollPane2.setManaged(false);

    }
    private String normalizeText(String text) {
        if (text == null) return "";
        // Supprime les espaces en début/fin, convertit tout en minuscules, remplace retours ligne par espaces,
        // et remplace multiples espaces par un seul espace.
        return text.trim()
                   .toLowerCase()
                   .replaceAll("\\r?\\n", " ")
                   .replaceAll("\\s+", " ");
    }
    
    @FXML
    private void onClickedChoicePdfCompare() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un deuxième PDF à comparer");
        File secondPdf = fileChooser.showOpenDialog(pdfScrollPane2.getScene().getWindow());
    
        if (secondPdf != null) {
            this.pdfFile2 = secondPdf;  // <--- Mise à jour de pdfFile2
            loadPdfToContainer(secondPdf, pdfPagesContainer2);
            LoggerService.log("PDF 2 chargé : " + secondPdf.getName());
        }
    }

    @FXML
    private void onClickedShowCompare() {
        boolean currentlyVisible = pdfScrollPane2.isVisible();
    
        pdfScrollPane2.setVisible(!currentlyVisible);
        pdfScrollPane2.setManaged(!currentlyVisible);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    

    public void comparePdfText(File pdfFile1, File pdfFile2) {
    try (PDDocument doc1 = PDDocument.load(pdfFile1);
         PDDocument doc2 = PDDocument.load(pdfFile2)) {

        int pages1 = doc1.getNumberOfPages();
        int pages2 = doc2.getNumberOfPages();

        int maxPages = Math.max(pages1, pages2);

        PDFTextStripper stripper = new PDFTextStripper();

        boolean allIdentical = true;

        for (int i = 1; i <= maxPages; i++) {
            String text1 = "";
            String text2 = "";

            if (i <= pages1) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                text1 = normalizeText(stripper.getText(doc1));
            }

            if (i <= pages2) {
                stripper.setStartPage(i);
                stripper.setEndPage(i);
                text2 = normalizeText(stripper.getText(doc2));
            }

            if (!text1.equals(text2)) {
                allIdentical = false;
                LoggerService.log("Différences détectées à la page " + i + " :");

                // Split en mots ou phrases courtes pour une meilleure comparaison
                String[] tokens1 = text1.split(" ");
                String[] tokens2 = text2.split(" ");

                int maxTokens = Math.max(tokens1.length, tokens2.length);

                for (int idx = 0; idx < maxTokens; idx++) {
                    String t1 = idx < tokens1.length ? tokens1[idx] : "";
                    String t2 = idx < tokens2.length ? tokens2[idx] : "";

                    if (!t1.equals(t2)) {
                        LoggerService.log(String.format("  Différence mot %d : '%s' vs '%s'", idx + 1, t1, t2));
                    }
                }
            } else {
                LoggerService.log("Page " + i + " identique.");
            }
        }

       
        
        
        
        if (allIdentical) {
            LoggerService.log("Les deux PDFs sont identiques (texte page par page).");
        } else {
            LoggerService.log("Les PDFs ont des différences détaillées.");
        }

    } catch (IOException e) {
        LoggerService.log("Erreur lors de la lecture des PDFs: " + e.getMessage());
    }
}
    // Méthodes pour assigner les fichiers pdfFile1 et pdfFile2, à intégrer selon ton app
    public void setPdfFiles(File file1, File file2) {
        this.pdfFile1 = file1;
        this.pdfFile2 = file2;
    }

    public boolean compareImagesPixelByPixel(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            return false; // tailles différentes -> pas identiques
        }
    
        int width = img1.getWidth();
        int height = img1.getHeight();
    
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false; // pixel différent détecté
                }
            }
        }
        return true; // toutes les pixels sont identiques
    }
    // Assure-toi que tu as un champ Stage (ou tu peux récupérer la fenêtre via un noeud)
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

  // === Gestion des préférences utilisateur (dossier PDF) ===
    private void saveLastPdfFolder(String path) {
        Preferences prefs = Preferences.userNodeForPackage(PdfController.class);
        prefs.put(PREF_KEY_LAST_PDF_FOLDER, path);
    }


    private String loadLastPdfFolder() {
        Preferences prefs = Preferences.userNodeForPackage(PdfController.class);
        return prefs.get("lastPdfFolder", null);
    }

  
    @FXML
    public void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choisir un dossier contenant des PDF");
        
        // Essayer de positionner au dernier dossier ouvert
        String lastFolderPath = prefs.get(LAST_FOLDER_KEY, null);
        if (lastFolderPath != null) {
            File lastFolder = new File(lastFolderPath);
            if (lastFolder.exists() && lastFolder.isDirectory()) {
                directoryChooser.setInitialDirectory(lastFolder);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            // Sauvegarder le chemin dans les prefs
            prefs.put(LAST_FOLDER_KEY, selectedDirectory.getAbsolutePath());

            // Met à jour la liste pdfFiles avec tous les pdf du dossier
            pdfFiles = Arrays.stream(selectedDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf")))
                             .sorted(Comparator.comparing(File::getName))
                             .collect(Collectors.toList());

            if (!pdfFiles.isEmpty()) {
                currentPdfIndex = 0;
                pdfFile1 = pdfFiles.get(currentPdfIndex);
                pdfFile2 = pdfFiles.get((currentPdfIndex + 1) % pdfFiles.size());
                openPdf(pdfFile1);
                System.out.println("Chargement du PDF : " + pdfFiles.get(currentPdfIndex).getName());
                openPdf(pdfFiles.get(currentPdfIndex));
            } else {
                System.out.println("Aucun PDF dans ce dossier.");
            }
        }
    }
    
 private Preferences prefs = Preferences.userNodeForPackage(PdfController.class);
    private static final String LAST_FOLDER_KEY = "lastPdfFolder";

    public void loadPdf(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
    
            // Clear previous content before loading new pages
            pdfPagesContainer1.getChildren().clear();
    
            // Loop through all pages to render and display them
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150); // render page i at 150 dpi
                Image fxImage = SwingFXUtils.toFXImage(bim, null);
    
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600); // or whatever width fits your UI
    
                pdfPagesContainer1.getChildren().add(imageView);
            }
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    private void openPdf(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            
            // On vide d'abord le conteneur pour afficher les pages
            pdfPagesContainer1.getChildren().clear();
            
            int pageCount = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                // Rendu de chaque page en image
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, 150);
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                
                ImageView imageView = new ImageView(fxImage);
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(600);  // ajuste largeur max
                
                pdfPagesContainer1.getChildren().add(imageView);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadPdfFilesFromFolder(File folder) {
        if (folder != null && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (files != null) {
                pdfFiles = Arrays.asList(files);
                currentPdfIndex = 0; // commencer au premier PDF
                if (!pdfFiles.isEmpty()) {
                    openPdf(pdfFiles.get(currentPdfIndex));
                }
            }
        }
    }
    

    @FXML
    private void changePdfFolder() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un deuxième PDF à comparer");
        File secondPdf = fileChooser.showOpenDialog(pdfScrollPane1.getScene().getWindow());
      
        if (secondPdf != null) {
            this.pdfFile1 = secondPdf;  // <--- Mise à jour de pdfFile2
            loadPdfToContainer(secondPdf, pdfPagesContainer1);
            LoggerService.log("PDF 2 chargé : " + secondPdf.getName());
        }
    }

    // Récupère le chemin du dernier dossier (null si pas défini)
    private String getLastPdfFolder() {
        Preferences prefs = Preferences.userNodeForPackage(PdfController.class);
        return prefs.get(PREF_KEY_LAST_PDF_FOLDER, null);
    }

  // À appeler lors de l'initialisation du controller (ex: méthode initialize)
  public void loadLastPdfFolderIfExists() {
    String lastFolderPath = prefs.get(LAST_FOLDER_KEY, null);
    if (lastFolderPath != null) {
        File lastFolder = new File(lastFolderPath);
        if (lastFolder.exists() && lastFolder.isDirectory()) {
            // Charge les PDFs dans ce dossier
            pdfFiles = Arrays.stream(lastFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf")))
                             .sorted(Comparator.comparing(File::getName))
                             .collect(Collectors.toList());

            if (!pdfFiles.isEmpty()) {
                currentPdfIndex = 0;
                System.out.println("Chargement du PDF : " + pdfFiles.get(currentPdfIndex).getName());
                openPdf(pdfFiles.get(currentPdfIndex));
            } else {
                System.out.println("Aucun PDF dans le dernier dossier enregistré.");
            }
        }
    }
}
    
    
    public void onChooseFolderButtonClicked() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(pdfPagesContainer1.getScene().getWindow());
        System.out.println("Dossier sélectionné : " + (selectedFolder != null ? selectedFolder.getAbsolutePath() : "aucun"));
        if (selectedFolder != null) {
            saveLastPdfFolder(selectedFolder.getAbsolutePath());
            openPdfFolder(selectedFolder);
        }
    }
    
    // Méthode que tu appelles quand un dossier est choisi par l'utilisateur
    public void onPdfFolderChosen(File folder) {
        saveLastPdfFolder(folder.getAbsolutePath());
        openPdfFolder(folder);
    }

    private void openPdfFolder(File folder) {
        loadPdfFolder(folder);
    }
    
    
    private void loadPdfFolder(File folder) {
        System.out.println("Chargement des PDFs dans : " + folder.getAbsolutePath());
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
        if (files == null || files.length == 0) {
            System.out.println("Aucun PDF dans le dossier " + folder.getAbsolutePath());
            return;
        }
        System.out.println(files.length + " fichiers PDF trouvés.");
        
        List<File> sortedFiles = Arrays.stream(files)
                                      .sorted(Comparator.comparingLong(File::lastModified).reversed())
                                      .collect(Collectors.toList());
        pdfFiles = sortedFiles;
        currentPdfFolder = folder;
        currentPdfIndex = 0;
    
        System.out.println("Ouverture du PDF : " + pdfFiles.get(currentPdfIndex).getName());
        openPdf(pdfFiles.get(currentPdfIndex));
    }
    @FXML
    private void onNextPdfButtonClicked() {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            System.out.println("Pas de PDF chargé");
            return;
        }
        // Si on est au dernier PDF, on revient au premier
        if (currentPdfIndex >= pdfFiles.size() - 1) {
            currentPdfIndex = 0;
        } else {
            currentPdfIndex++;
        }
        System.out.println("Ouverture PDF: " + pdfFiles.get(currentPdfIndex).getName());
        openPdf(pdfFiles.get(currentPdfIndex));
        if (currentPdfIndex < pdfFiles.size() - 1) {
            currentPdfIndex++;
            pdfFile1 = pdfFiles.get(currentPdfIndex);
            pdfFile2 = pdfFiles.get((currentPdfIndex + 1) % pdfFiles.size());
            openPdf(pdfFile1);
        }
        
    }
    

    @FXML
    private void onPreviousPdfButtonClicked() {
        if (pdfFiles == null || pdfFiles.isEmpty()) {
            System.out.println("Pas de PDF chargé");
            return;
        }
        // Si on est au premier PDF, on revient au dernier
        if (currentPdfIndex <= 0) {
            currentPdfIndex = pdfFiles.size() - 1;
        } else {
            currentPdfIndex--;
        }
        System.out.println("Ouverture PDF: " + pdfFiles.get(currentPdfIndex).getName());
        openPdf(pdfFiles.get(currentPdfIndex));
        if (currentPdfIndex > 0) {
            currentPdfIndex--;
            pdfFile1 = pdfFiles.get(currentPdfIndex);
            pdfFile2 = pdfFiles.get((currentPdfIndex + 1) % pdfFiles.size());
            openPdf(pdfFile1);
        }
        
    }
    

    
    
    public void comparePdfByPixels(File pdfFile1, File pdfFile2) throws IOException {
        try (PDDocument doc1 = PDDocument.load(pdfFile1);
             PDDocument doc2 = PDDocument.load(pdfFile2)) {
    
            int pages1 = doc1.getNumberOfPages();
            int pages2 = doc2.getNumberOfPages();
            int maxPages = Math.max(pages1, pages2);
    
            PDFRenderer renderer1 = new PDFRenderer(doc1);
            PDFRenderer renderer2 = new PDFRenderer(doc2);
    
            boolean allPagesIdentical = true;
    
            for (int i = 0; i < maxPages; i++) {
                if (i >= pages1 || i >= pages2) {
                    System.out.println("Nombre de pages différent entre les deux PDFs.");
                    allPagesIdentical = false;
                    break;
                }
    
                BufferedImage img1 = renderer1.renderImageWithDPI(i, 150, ImageType.RGB);
                BufferedImage img2 = renderer2.renderImageWithDPI(i, 150, ImageType.RGB);
    
                boolean pageIdentique = compareImagesPixelByPixel(img1, img2);
                if (!pageIdentique) {
                    System.out.println("Différence détectée à la page " + (i + 1));
                    allPagesIdentical = false;
                } else {
                    System.out.println("Page " + (i + 1) + " identique.");
                }
            }
    
            if (allPagesIdentical) {
                System.out.println("Les deux PDFs sont identiques en comparant pixel par pixel.");
            } else {
                System.out.println("Des différences ont été trouvées entre les PDFs.");
            }
        }
    }
    
    
    private void loadPdfFromFolder() {
        if (currentPdfFolder == null || pdfFiles == null || pdfFiles.isEmpty()) return;
        openPdf(pdfFiles.get(currentPdfIndex));
    }

    @FXML
    private void onToggleImageOverlay() {
        overlayMode = !overlayMode;
        if (overlayMode) {
            if (pdfFile1 == null || pdfFile2 == null) {
                System.out.println("Erreur : pdfFile1 ou pdfFile2 non initialisé");
                return;
            }
            try {
                showImageComparison(pdfFile1, pdfFile2);
                pdfScrollPane2.setVisible(false);
                pdfScrollPane2.setManaged(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            loadPdfToContainer(pdfFile1, pdfPagesContainer1);
            loadPdfToContainer(pdfFile2, pdfPagesContainer2);
            pdfScrollPane2.setVisible(true);
            pdfScrollPane2.setManaged(true);
        }
    }
    
    private void showImageComparison(File pdfFile1, File pdfFile2) throws IOException {
        if (pdfFile1 == null || pdfFile2 == null) {
            LoggerService.log("Un des fichiers PDF est null, impossible de comparer les images.");
            return;
        }
    
        // Rendre la première page de chaque PDF en image
        Image img1 = renderPdfPageAsImage(pdfFile1, 0);
        Image img2 = renderPdfPageAsImage(pdfFile2, 0);
    
        if (img1 == null || img2 == null) {
            LoggerService.log("Erreur de rendu d'image pour les PDFs.");
            return;
        }
    
        // ImageView pour PDF1
        ImageView iv1 = new ImageView(img1);
        iv1.setFitWidth(baseWidth * scale1);
        iv1.setPreserveRatio(true);
    
        // ImageView pour PDF2 avec opacité (superposition)
        ImageView iv2 = new ImageView(img2);
        iv2.setFitWidth(baseWidth * scale2);
        iv2.setPreserveRatio(true);
        iv2.setOpacity(0.5); // 50% transparent pour voir les différences
    
        // StackPane pour superposition
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(iv1, iv2);
    
        // Nettoyer le container et afficher la superposition
        pdfPagesContainer1.getChildren().clear();
        pdfPagesContainer1.getChildren().add(stackPane);
    }
    
    @FXML
    private void zoomIn() {
        if (activePdfPagesContainer == pdfPagesContainer1) {
            scale1 = Math.min(scale1 + scaleStep, maxScale);
            applyZoom(pdfPagesContainer1, scale1);
        } else if (activePdfPagesContainer == pdfPagesContainer2) {
            scale2 = Math.min(scale2 + scaleStep, maxScale);
            applyZoom(pdfPagesContainer2, scale2);
        }
    }

    @FXML
    private void onClickedComparePdf() {
        if (pdfFile1 != null && pdfFile2 != null) {
            
            LoggerService.log("Démarrage de la comparaison des PDFs :");
            LoggerService.log("PDF 1 : " + pdfFile1.getName());
            LoggerService.log("PDF 2 : " + pdfFile2.getName());
    
            // Compare textuel et log
            comparePdfText(pdfFile1, pdfFile2);
    
            // Optionnel : continuer à afficher les PDFs
            loadPdfToContainer(pdfFile1, pdfPagesContainer1);
            loadPdfToContainer(pdfFile2, pdfPagesContainer2);
    
            LoggerService.log("Comparaison lancée pour les deux PDFs.");
        } else {
            LoggerService.log("Erreur : les fichiers PDF ne sont pas définis.");
        }
    }
    

    @FXML
    private void zoomOut() {
        if (activePdfPagesContainer == pdfPagesContainer1) {
            scale1 = Math.max(scale1 - scaleStep, minScale);
            applyZoom(pdfPagesContainer1, scale1);
        } else if (activePdfPagesContainer == pdfPagesContainer2) {
            scale2 = Math.max(scale2 - scaleStep, minScale);
            applyZoom(pdfPagesContainer2, scale2);
        }
    }

    private void applyZoom(VBox container, double scale) {
        for (Node node : container.getChildren()) {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                imageView.setFitWidth(baseWidth * scale);
            }
        }
    }
    
    
    private Image renderPdfPageAsImage(File pdfFile, int pageIndex) throws IOException {
        if (pdfFile == null || !pdfFile.exists()) {
            System.err.println("Erreur : fichier PDF est null ou introuvable.");
            return null;
        }
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bufferedImage = renderer.renderImageWithDPI(pageIndex, 150, ImageType.RGB);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        }
    }
    
    private void updateScale() {
        if (activePdfPagesContainer == null) return;
    
        for (Node node : activePdfPagesContainer.getChildren()) {
            if (node instanceof ImageView) {
                ImageView iv = (ImageView) node;
                iv.setFitWidth((pdfScrollPane1.getWidth() - 20) * scale1);
            }
        }

        if (pdfPagesContainer1 != null) {
            for (Node node : pdfPagesContainer1.getChildren()) {
                if (node instanceof ImageView) {
                    ((ImageView) node).setFitWidth((pdfScrollPane1.getWidth() - 20) * scale1);
                }
            }
        }
        if (pdfPagesContainer2 != null) {
            for (Node node : pdfPagesContainer2.getChildren()) {
                if (node instanceof ImageView) {
                    ((ImageView) node).setFitWidth((pdfScrollPane2.getWidth() - 20) * scale2);
                }
            }
        }
    }        
    
    @FXML
    private void onChangePdfFolderButtonClicked() {
        // Ouvre un dialog pour choisir un nouveau dossier ou fichier PDF
        // puis charge le PDF
        System.out.println("Bouton changer de fichier cliqué !");
        
        // Par exemple : ouvrir un FileChooser (ou DirectoryChooser)
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(rootCenterView.getScene().getWindow());
        if (selectedFile != null) {
            afficherPdf(selectedFile.getName());
        }
}

    
private void loadPdfToContainer(File pdfFile, VBox container) {
    if (pdfFile == null || !pdfFile.exists()) {
        LoggerService.log("Fichier PDF non trouvé pour affichage : " + (pdfFile != null ? pdfFile.getPath() : "null"));
        return;
    }
    try (PDDocument document = PDDocument.load(pdfFile)) {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        container.getChildren().clear();

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
            Image fxImage = SwingFXUtils.toFXImage(bim, null);
            ImageView iv = new ImageView(fxImage);
            iv.setFitWidth(baseWidth * scale1);
            iv.setPreserveRatio(true);
            container.getChildren().add(iv);
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

}



}
