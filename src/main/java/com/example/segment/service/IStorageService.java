package com.example.segment.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * created on: 15/05/23
 * created by: harsha
 */
public interface IStorageService {
    void putFile(MultipartFile file) throws IOException;
    void putFile(MultipartFile file, String name) throws IOException;
    Resource getFile(String fileName);
    void putImageWithSegments(MultipartFile original, List<MultipartFile>segments, String groupName);
}
