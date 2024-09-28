/**
 * 
 */
package com.ibs.service;

/**
 * 
 */
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class VideoTranscoderService {

	@Value("${ffmpeg.path}")
	private String ffmpegPath;

	@Value("${ffprobe.path}")
	private String ffprobePath;

	@Value("${video.output.directory}")
	private String outputDirectory;

	public void transcodeToHLS(String inputDirectory) throws IOException {
		FFmpeg ffmpeg = new FFmpeg(ffmpegPath);
		FFprobe ffprobe = new FFprobe(ffprobePath);

		Files.list(Paths.get(inputDirectory)).filter(path -> path.toString().toLowerCase().endsWith(".mkv") 
				/*|| path.toString().toLowerCase().endsWith(".mkv")*/
				).forEach(path -> {
			String inputFilePath = path.toString();
			String fileName = path.getFileName().toString();
			String outputDir = outputDirectory + "/" + fileName.substring(0, fileName.lastIndexOf('.'));
			File outputDirFile = new File(outputDir);
			if (!(outputDirFile.exists() && outputDirFile.listFiles().length > 0)) {
				new File(outputDir).mkdirs(); // Crée un répertoire pour chaque fichier vidéo

				FFmpegBuilder builder = new FFmpegBuilder().setInput(inputFilePath).addOutput(outputDir + "/index.m3u8")
						.setFormat("hls").addExtraArgs("-hls_time", "10").addExtraArgs("-hls_list_size", "0")
						.addExtraArgs("-hls_segment_filename", outputDir + "/seg_%03d.ts").done();

				FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
				executor.createJob(builder).run();
			}
		});
	}
}
