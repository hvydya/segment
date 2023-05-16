package com.example.segment.service;

/**
 * created on: 15/05/23
 * created by: harsha
 */
public interface ISegmentService {

    int[][] getSegmentMap(String groupName);
    void putSegmentMap(int[][] segmentMap, String groupName);
    boolean doesGroupExist(String groupName);

}
