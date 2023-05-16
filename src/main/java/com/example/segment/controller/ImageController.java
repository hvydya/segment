package com.example.segment.controller;

import com.example.segment.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * created on: 15/05/23
 * created by: harsha
 */
@Controller
public class ImageController {

    @Autowired
    IStorageService storageService;

    @Autowired
    ImageProcessor imageProcessor;

    @Autowired
    ISegmentService segmentService;

    @PostMapping("/api/images")
    public ResponseEntity<String> postImages(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName
    ) throws IOException {
        storageService.putFile(file, fileName);
        return ResponseEntity.ok("uploaded");
    }

    /**
     * Only PNGs!!
     * Saves the original image in the path <IMAGE_PATH>/groupName/0.png
     * Saves the segments as 1.png, 2.png, ...etc at <IMAGE_PATH>/groupName
     *
     * After saving the files, it builds a segment map which is basically a 2d array of the same size as the image
     * Then for each segment if the pixel value at a particular coordinate is non-zero then in the segment map we mark
     * that coordinate with this segment's file name i.e 1,2,3,4 etc.
     */
    @PostMapping("/api/images/segments")
    public ResponseEntity<String> postImagesWithSegments(
            @RequestParam("original") MultipartFile original,
            String groupName,
            @RequestParam("segments") List<MultipartFile> segments
    ) throws IOException {
        if (segmentService.doesGroupExist(groupName)) {
            return ResponseEntity.badRequest().body("Group already exists!");
        }
        // save the files
        storageService.putImageWithSegments(original, segments, groupName);
        // build segment map
        int[][] segmentMap = imageProcessor.buildSegmentMap(groupName, segments.size());
        segmentService.putSegmentMap(segmentMap, groupName);
        return ResponseEntity.ok(String.format("uploaded to %s", imageProcessor.imageStoragePath + groupName));
    }

    @GetMapping("/api/images")
    public ResponseEntity<Resource> serveFile(@RequestParam("file") String fileName) {
        Resource file = storageService.getFile(fileName);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/api/segment")
    public ResponseEntity<String> getSegmentForGroup(@RequestParam("group") String groupName, @RequestParam("x") int x, @RequestParam("y") int y) throws IOException, ClassNotFoundException {
        if (!segmentService.doesGroupExist(groupName)) {
            return ResponseEntity.badRequest().body("Group doesn't exist");
        }
        int[][] segmentMap = segmentService.getSegmentMap(groupName);
        if (x >= segmentMap.length || y >= segmentMap[0].length || x < 0 || y < 0) {
            return ResponseEntity.badRequest().body(String.format("coordinates are crossing maximum permissible boundaries: [%d, %d]", segmentMap.length, segmentMap[0].length));
        }
        return ResponseEntity.ok(String.format("Belongs to segment " + imageProcessor.imageStoragePath + groupName + "/%s.png", segmentMap[x][y]));
    }

}
