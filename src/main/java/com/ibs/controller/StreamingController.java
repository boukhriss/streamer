/**
 * 
 */
package com.ibs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stream")
public class StreamingController {

	@Value("${video.output.directory}")
	private String outputDirectory;

	private static final Logger logger = LoggerFactory.getLogger(StreamingController.class);

	@GetMapping("/videos")
	public List<String> listVideos() throws IOException {
		return Files.list(Paths.get(outputDirectory)).filter(Files::isDirectory).map(Path::getFileName)
				.map(Path::toString).collect(Collectors.toList());
	}

	@Cacheable("hlsStream")
	@GetMapping(value = "/hls/{videoName}/index.m3u8", produces = "application/vnd.apple.mpegurl")
	public ResponseEntity<byte[]> getHLSStream(@PathVariable("videoName") String videoName) throws IOException {
		try {
			File file = new File(outputDirectory + "/" + videoName + "/index.m3u8");
			if (!file.exists() || !file.isFile()) {
				logger.error("File not found: " + file.getAbsolutePath());
				return ResponseEntity.status(404).build();
			}
			InputStream inputStream = new FileInputStream(file);
			byte[] data = StreamUtils.copyToByteArray(inputStream);

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl").body(data);
		} catch (IOException e) {
			logger.error("Error reading HLS stream", e);
			return ResponseEntity.status(500).build();
		}
	}

	@Cacheable("hlsSegment")
	@GetMapping(value = "/hls/{videoName}/{segmentName}", produces = "video/MP2T")
	public ResponseEntity<byte[]> getHLSSegment(@PathVariable("videoName") String videoName,
			@PathVariable("segmentName") String segmentName) throws IOException {
		try {
			File file = new File(outputDirectory + "/" + videoName + "/" + segmentName);
			if (!file.exists() || !file.isFile()) {
				logger.error("Segment not found: " + file.getAbsolutePath());
				return ResponseEntity.status(404).build();
			}
			InputStream inputStream = new FileInputStream(file);
			byte[] data = StreamUtils.copyToByteArray(inputStream);

			return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "video/MP2T").body(data);
		} catch (IOException e) {
			logger.error("Error reading HLS segment: " + segmentName + " for video: " + videoName, e);
			return ResponseEntity.status(500).build();
		}
	}
}
