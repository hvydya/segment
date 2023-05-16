package com.example.segment.service;

import lombok.AllArgsConstructor;

import java.io.*;

/**
 * created on: 15/05/23
 * created by: harsha
 */
@AllArgsConstructor
public class AsyncProcessor implements Runnable {

    ImageProcessor processor;
    String groupName;
    int segments;
    ISegmentService segmentService;

    @Override
    public void run() {
        try {
            int[][] segmentMap = processor.buildSegmentMap(groupName, segments);
            segmentService.putSegmentMap(segmentMap, groupName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
