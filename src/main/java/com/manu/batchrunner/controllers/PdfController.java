package com.manu.batchrunner.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PdfController implements Initializable {

    @FXML
    private ImageView pdfImageView;

    @FXML
    private ScrollPane pdfScrollPane; 

    @FXML
    private VBox pdfPagesContainer;

    @FXML
    private Button zoomOutButton;

    @FXML
    private Button zoomInButton;

    @FXML
    private VBox rootCenterView; 

    private double scale = 1.0;
    private final double scaleStep = 0.1;
    private final double minScale = 0.3;
    private final double maxScale = 3.0;

    private List<ImageView> pageImageViews = new ArrayList<>();

    private final double baseWidth = 600; 
        
    public void afficherPdf(String cheminPdf) {
        try (PDDocument document = PDDocument.load(new File(cheminPdf))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pdfPagesContainer.getChildren().clear();
            pageImageViews.clear();
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                Image fxImage = SwingFXUtils.toFXImage(bim, null);
                ImageView iv = new ImageView(fxImage);
            
                iv.setFitWidth(baseWidth);

                iv.setPreserveRatio(true);
                
                pdfPagesContainer.getChildren().add(iv);
                pageImageViews.add(iv);   // <-- Ajoute ici
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        
         // Accélérer la vitesse du scroll classique (sans Ctrl)
        pdfScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!event.isControlDown()) { // On ne modifie pas le scroll avec Ctrl (zoom)
                // Multiplier la valeur deltaY par un facteur (exemple 3x plus rapide)
                double deltaY = event.getDeltaY() * 5;

                // Simuler un nouveau scroll avec ce delta plus grand
                double newVvalue = pdfScrollPane.getVvalue() - deltaY / pdfPagesContainer.getHeight();

                // Limiter newVvalue entre 0 et 1
                if (newVvalue < 0) newVvalue = 0;
                if (newVvalue > 1) newVvalue = 1;

                pdfScrollPane.setVvalue(newVvalue);

                event.consume(); // Empêche le comportement par défaut
            }
        });
    }

     
    @FXML
    private void zoomIn() {
        if (scale + scaleStep <= maxScale) {
            scale += scaleStep;
            updateScale();
        }
    }

    @FXML
    private void zoomOut() {
        if (scale - scaleStep >= minScale) {
            scale -= scaleStep;
            updateScale();
        }
    }

    private void updateScale() {
        for (ImageView iv : pageImageViews) {
            iv.setFitWidth((pdfScrollPane.getWidth() - 20) * scale);
        }
    }
    
    
}
