package com.example.segment.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * created on: 16/05/23
 * created by: harsha
 */
@Service
public class RunLengthCompression implements ICompressionService {

    public int[][] getArrayFromList(List<List<Integer>> list) {
        int[][] arr = new int[list.size()][];
        int i = 0;
        for (List<Integer> l : list) {
            arr[i] = new int[l.size()];
            for (int j = 0; j < l.size(); j++) arr[i][j] = l.get(j);
            i++;
        }
        return arr;
    }

    public int[][] encode(int[][] map) {
        List<List<Integer>> encoded = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            List<Integer> current = new ArrayList<>();
            int prev = Integer.MIN_VALUE;
            int c = 1;
            for (int j = 0; j < map[i].length; j++) {
                if (prev == map[i][j]) {
                    c++;
                } else if (prev != Integer.MIN_VALUE) {
                    current.add(prev);
                    current.add(c);
                    c = 1;
                }
                prev = map[i][j];
            }
            if (prev != Integer.MIN_VALUE) {
                current.add(prev);
                current.add(c);
            }
            encoded.add(current);
        }

        return getArrayFromList(encoded);
    }

    public int[][] decode(int[][] encoded) {
        List<List<Integer>> map = new ArrayList<>();
        for (int[] currentRow : encoded) {
            List<Integer> decodedRow = new ArrayList<>();
            for (int j = 0; j < currentRow.length; j += 2) {
                for (int x = 0; x < currentRow[j + 1]; x++) {
                    decodedRow.add(currentRow[j]);
                }
            }
            map.add(decodedRow);
        }
        int[][] decoded = new int[map.size()][];
        for (int i = 0; i < map.size(); i++) {
            decoded[i] = new int[map.get(i).size()];
            for (int j = 0; j < decoded[i].length; j++) {
                decoded[i][j] = map.get(i).get(j);
            }
        }
        return decoded;
    }

    @Override
    public byte[] compressSegmentMap(int[][] segmentMap) throws CompressionFailureException {
        int[][] compressed = encode(segmentMap);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(compressed);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new CompressionFailureException(e);
        }
    }

    @Override
    public int[][] decompressSegmentMap(byte[] compressedBytes) throws DecompressionFailureException {
        ObjectInputStream iis = null;
        try {
            iis = new ObjectInputStream(new ByteArrayInputStream(compressedBytes));
            int[][] encoded = (int[][]) iis.readObject();
            iis.close();
            return decode(encoded);
        } catch (IOException | ClassNotFoundException e) {
            throw new DecompressionFailureException(e);
        }
    }

    public static class CompressionFailureException extends Exception {
        public CompressionFailureException(Exception e) {
            super(e.getMessage());
        }
    }

    public static class DecompressionFailureException extends Exception {
        public DecompressionFailureException(Exception e) {
            super(e.getMessage());
        }
    }
}
