package com.example.segment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * created on: 15/05/23
 * created by: harsha
 */
@Service
@Slf4j
public class DiskStorageService implements IStorageService {

    @Value("${image.storage.path}")
    public String imageStoragePath;

    @Override
    public void putFile(MultipartFile file) throws IOException {
        String name = file.getName();
        putFile(file, name);
    }

    @Override
    public void putFile(MultipartFile file, String name) throws IOException {
        String p = imageStoragePath + name;
        Path path = Paths.get(p);
        Files.createDirectories(path.getParent());
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        log.info("Written file {}", p);
    }

    @Override
    public Resource getFile(String fileName) {
        String path = imageStoragePath + fileName;
        return new PathResource(Paths.get(path));
    }

    @Override
    public void putImageWithSegments(MultipartFile original, List<MultipartFile> segments, String groupName) {
        String pathSuffix = groupName + "/%d.png";
        try {
            putFile(original, String.format(pathSuffix, 0));
            int i = 1;
            for (MultipartFile file : segments) {
                putFile(file, String.format(pathSuffix, i++));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
