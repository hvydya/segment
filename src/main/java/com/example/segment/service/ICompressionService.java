package com.example.segment.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * created on: 16/05/23
 * created by: harsha
 */
public interface ICompressionService {

    byte[] compressSegmentMap(int[][] segmentMap) throws RunLengthCompression.CompressionFailureException;
    int[][] decompressSegmentMap(byte[] compressedBytes) throws RunLengthCompression.DecompressionFailureException;
}
