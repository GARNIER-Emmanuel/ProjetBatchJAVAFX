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

    private double scale = 1.0;
    private final double scaleStep = 0.1;
    private final double minScale = 0.3;
    private final double maxScale = 3.0;

    private List<ImageView> pageImageViews = new ArrayList<>();

        
    public void afficherPdf(String cheminPdf) {
        try (PDDocument document = PDDocument.load(new File(cheminPdf))) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pdfPagesContainer.getChildren().clear();

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 150, ImageType.RGB);
                Image fxImage = SwingFXUtils.toFXImage(bim, null);
                ImageView iv = new ImageView(fxImage);

                // Ajuste la largeur à celle du ScrollPane pour un meilleur rendu
                iv.setFitWidth(pdfScrollPane.getWidth() - 20);
                iv.setPreserveRatio(true);

                pdfPagesContainer.getChildren().add(iv);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String cheminPdf = "C:/Users/manub/OneDrive/Desktop/pdf/mamie.pdf";
        afficherPdf(cheminPdf);
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
