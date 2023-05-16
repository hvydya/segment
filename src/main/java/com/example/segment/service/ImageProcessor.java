package com.example.segment.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;


/**
 * created on: 15/05/23
 * created by: harsha
 */
@Service
public class ImageProcessor {

    @Value("${image.storage.path}")
    public String imageStoragePath;

    public void processImage(int[][] marker, String path, int markerToken) {
        //Reading the image
        File file= new File(path);
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int j = 0; j < img.getHeight(); j++) {
            for (int i = 0; i < img.getWidth(); i++) {
                int pixel = img.getRGB(i, j);
                if (pixel != 0) {
                    marker[i][j] = markerToken;
                }
            }
        }
    }

    public int[][] buildSegmentMap(String groupName, int segments) throws IOException {
        String pathTemplate = imageStoragePath + groupName + "/%d.png";
        Pair<Integer, Integer> dimensions = getImageDimensions(groupName);
        int[][] mat = new int[dimensions.getLeft()][dimensions.getRight()];
        for (int i = 1; i <= segments; i++) {
            processImage(mat, String.format(pathTemplate, i), i);
        }
        return mat;
    }

    public Pair<Integer, Integer> getImageDimensions(String groupName) {
        String path = imageStoragePath + groupName + "/0.png";
        BufferedImage img = null;
        try {
            File file= new File(path);
            img = ImageIO.read(file);
            return Pair.of(img.getWidth(), img.getHeight());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
