package com.example.segment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * created on: 15/05/23
 * created by: harsha
 */
@Service
public class DiskSegmentService implements ISegmentService {

    @Value("${image.storage.path}")
    public String imageStoragePath;

    @Autowired
    ICompressionService compressionService;

    public String getSegmentMapPath(String groupName) {
        return imageStoragePath + groupName + "/map";
    }

    @Override
    public int[][] getSegmentMap(String groupName) {
        if (!doesGroupExist(groupName)) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(getSegmentMapPath(groupName)));
            return compressionService.decompressSegmentMap(bytes);
        } catch (IOException | RunLengthCompression.DecompressionFailureException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void putSegmentMap(int[][] segmentMap, String groupName) {
        try {
            // compressing the map so that we can save some space.
            Files.write(Paths.get(getSegmentMapPath(groupName)), compressionService.compressSegmentMap(segmentMap), StandardOpenOption.CREATE);
        } catch (IOException | RunLengthCompression.CompressionFailureException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean doesGroupExist(String groupName) {
        return Files.exists(Paths.get(getSegmentMapPath(groupName)), LinkOption.NOFOLLOW_LINKS);
    }
}
