package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import com.manu.batchrunner.utils.LoggerService;

import javafx.scene.Node;
import org.apache.pdfbox.text.PDFTextStripper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PdfController implements Initializable {


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
    private ScrollPane pdfScrollPane2;

    private File pdfFile1; // À assigner avec le PDF 1
    private File pdfFile2; // À assigner avec le PDF 2
    @FXML
    private Button changePdfFolderButton;
    private File currentPdfFolder;
    
    private double scale = 1.0;
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

        pdfScrollPane2.setVisible(false);
        pdfScrollPane2.setManaged(false);
    
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

        String cheminPdf = "C:/Users/manub/OneDrive/Desktop/pdf/mamie.pdf";
        afficherPdf(cheminPdf);

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
               text1 = stripper.getText(doc1).trim();
           }

           if (i <= pages2) {
               stripper.setStartPage(i);
               stripper.setEndPage(i);
               text2 = stripper.getText(doc2).trim();
           }

           if (!text1.equals(text2)) {
               allIdentical = false;
               LoggerService.log("Différences détectées à la page " + i + " :");

               // Split en lignes
               String[] lines1 = text1.split("\\r?\\n");
               String[] lines2 = text2.split("\\r?\\n");

               int maxLines = Math.max(lines1.length, lines2.length);

               for (int lineIndex = 0; lineIndex < maxLines; lineIndex++) {
                   String line1 = lineIndex < lines1.length ? lines1[lineIndex] : "";
                   String line2 = lineIndex < lines2.length ? lines2[lineIndex] : "";

                   if (!line1.equals(line2)) {
                       LoggerService.log(String.format("  Ligne %d différente :", lineIndex + 1));
                       LoggerService.log("    PDF 1 : " + line1);
                       LoggerService.log("    PDF 2 : " + line2);
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
    private void loadPdfFromFolder() {
        if (currentPdfFolder == null) return;
    
    }

    @FXML
    private void zoomIn() {
        if (scale + scaleStep <= maxScale) {
            scale += scaleStep;
            updateScale();
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
        if (scale - scaleStep >= minScale) {
            scale -= scaleStep;
            updateScale();
        }
    }

    private Image renderPdfPageAsImage(File pdfFile, int pageIndex) throws IOException {
        if (pdfFile == null || !pdfFile.exists()) {
            System.err.println("Erreur : fichier PDF est null ou introuvable.");
            return null; // Ou une image par défaut, ou lever une exception personnalisée
        }
    
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage bImg = renderer.renderImageWithDPI(pageIndex, 150);
            return SwingFXUtils.toFXImage(bImg, null);
        }
    }
    
    private void updateScale() {
        if (activePdfPagesContainer == null) return;
    
        for (Node node : activePdfPagesContainer.getChildren()) {
            if (node instanceof ImageView) {
                ImageView iv = (ImageView) node;
                iv.setFitWidth((pdfScrollPane1.getWidth() - 20) * scale);
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
    try (PDDocument document = PDDocument.load(pdfFile)) {
        PDFRenderer pdfRenderer = new PDFRenderer(document);
        container.getChildren().clear();

        for (int page = 0; page < document.getNumberOfPages(); ++page) {
            BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
            Image fxImage = SwingFXUtils.toFXImage(bim, null);
            ImageView iv = new ImageView(fxImage);

            iv.setPreserveRatio(true);
            iv.setFitWidth(baseWidth * scale); // adapte à l’échelle de zoom actuelle

            container.getChildren().add(iv);
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
