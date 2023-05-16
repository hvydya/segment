package com.example.segment;

import com.example.segment.service.DiskSegmentService;
import com.example.segment.service.ISegmentService;
import com.example.segment.service.ImageProcessor;
import com.example.segment.service.RunLengthCompression;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
@TestPropertySource("classpath:test.properties")
class SegmentApplicationTests {

	@Autowired
	ImageProcessor imageProcessor;

	int[][] getExpected(String path, int marker, int size) {
		int[][] mat = new int[size][size];
		//Reading the image
		File file= new File(path);
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				// Retrieving contents of a pixel
				int pixel = img.getRGB(x,y);
//                writer.append(pixel + " ");
				if (pixel != 0) {
					mat[x][y] = marker;
				}
			}
		}
		return mat;
	}

	@Test
	void testImageProcessor() throws IOException {
		int[][] coords = imageProcessor.buildSegmentMap("testcase", 16);
		for (int page = 1; page < 17; page++) {
			int[][] expected = getExpected(String.format(imageProcessor.imageStoragePath + "testcase/%d.png", page), page, 2048);
			int match = 0;
			for (int i = 0; i < coords.length; i++) {
				for (int j = 0; j < coords[i].length; j++) {
					if ((expected[i][j] == 0 && page != coords[i][j]) || expected[i][j] == coords[i][j]) {
						match++;
					}
				}
			}
			double percent = (match * 100.0) / (2048 * 2048);
			log.info("Got {} for {}", percent, page);
			Assertions.assertTrue(Double.compare(percent, 99.0) >= 0, "got " + percent + " for " + page);
		}
	}

	@Test
	void testDeserialize() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream("src/test/resources/expected_map");
		ObjectInputStream iis = new ObjectInputStream(fis);
		int[][] got = (int[][]) iis.readObject();
		int[][] expected = imageProcessor.buildSegmentMap("testcase", 16);
		for (int i = 0; i < expected.length; i++) {
			for (int j = 0; j < expected[i].length; j++) {
				Assertions.assertEquals(expected[i][j], got[i][j], i + " " + j);
			}
		}
	}

	@Autowired
	RunLengthCompression runLengthCompression;

	@Test
	void testEncodeDecode() {
		int[][] tc1 = new int[][]{{1,2,3,4,5},{6,7,8,9,10}};
		int[][] expected1 = new int[][]{{1,1,2,1,3,1,4,1,5,1}, {6,1,7,1,8,1,9,1,10,1}};
		int[][] tc2 = new int[][]{{1,1,1,1,1},{2,2,2,2,2},{3,3,3,4,4}};
		int[][] expected2 = new int[][]{{1,5},{2,5},{3,3,4,2}};
		Assertions.assertArrayEquals(expected1, runLengthCompression.encode(tc1));
		Assertions.assertArrayEquals(expected2, runLengthCompression.encode(tc2));
		Assertions.assertArrayEquals(tc1, runLengthCompression.decode(expected1));
		Assertions.assertArrayEquals(tc2, runLengthCompression.decode(expected2));
	}

	@Test
	void testCompressDecompress() throws RunLengthCompression.CompressionFailureException, RunLengthCompression.DecompressionFailureException, IOException, ClassNotFoundException {
		int[][] tc1 = new int[][]{{1,2,3,4,5},{6,7,8,9,10}};
		byte[] bytes = runLengthCompression.compressSegmentMap(tc1);
		int[][] got1 = runLengthCompression.decompressSegmentMap(bytes);

		Assertions.assertArrayEquals(tc1, got1);

		int[][] tc2 = new int[][]{{1,1,1,1,1},{2,2,2,2,2},{3,3,3,4,4}};

		bytes = runLengthCompression.compressSegmentMap(tc2);
		int[][] got2 = runLengthCompression.decompressSegmentMap(bytes);

		Assertions.assertArrayEquals(tc2, got2);

		FileInputStream fis = new FileInputStream("src/test/resources/expected_map");
		ObjectInputStream iis = new ObjectInputStream(fis);
		int[][] tc3 = (int[][]) iis.readObject();
		bytes = runLengthCompression.compressSegmentMap(tc3);
		int[][] got3 = runLengthCompression.decompressSegmentMap(bytes);

		Assertions.assertArrayEquals(tc3, got3);
	}

	@Autowired
	DiskSegmentService segmentService;

	@Test
	void testDiskSegmentWithCompression() throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream("src/test/resources/expected_map");
		ObjectInputStream iis = new ObjectInputStream(fis);
		int[][] expected = (int[][]) iis.readObject();
		segmentService.putSegmentMap(expected, "testcase");
		Assertions.assertArrayEquals(expected, segmentService.getSegmentMap("testcase"));

		FileInputStream compressedFile = new FileInputStream(segmentService.imageStoragePath + "testcase/map");
		Assertions.assertTrue(Files.readAllBytes(Paths.get(segmentService.imageStoragePath + "testcase/map")).length < Files.readAllBytes(Paths.get("src/test/resources/expected_map")).length);
		fis.close();
		compressedFile.close();
	}
}
