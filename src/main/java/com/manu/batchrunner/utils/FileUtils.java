package com.manu.batchrunner.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    /**
     * Lit un fichier texte et renvoie son contenu sous forme de String.
     * @param filePath chemin complet du fichier
     * @return contenu du fichier
     * @throws IOException si erreur de lecture
     */
    public static String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Path.of(filePath)));
    }

    /**
     * Écrit une chaîne de caractères dans un fichier texte.
     * @param filePath chemin complet du fichier
     * @param content contenu à écrire
     * @throws IOException si erreur d’écriture
     */
    public static void writeStringToFile(String filePath, String content) throws IOException {
        Files.writeString(Path.of(filePath), content);
    }

    /**
     * Vérifie si un fichier existe.
     * @param filePath chemin complet du fichier
     * @return true si existe, false sinon
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Path.of(filePath));
    }

    /**
     * Crée un dossier si il n'existe pas.
     * @param dirPath chemin complet du dossier
     * @throws IOException si erreur de création
     */
    public static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Path.of(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * Copie un fichier vers une nouvelle destination.
     * @param sourcePath chemin source
     * @param targetPath chemin cible
     * @throws IOException si erreur de copie
     */
    public static void copyFile(String sourcePath, String targetPath) throws IOException {
        Files.copy(Path.of(sourcePath), Path.of(targetPath), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Supprime un fichier.
     * @param filePath chemin du fichier
     * @throws IOException si erreur suppression
     */
    public static void deleteFile(String filePath) throws IOException {
        Files.deleteIfExists(Path.of(filePath));
    }
}
