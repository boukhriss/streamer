package com.ibs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ibs.service.VideoTranscoderService;

@SpringBootApplication
public class StreamingServerApplication implements CommandLineRunner {

    @Autowired
    private VideoTranscoderService videoTranscoderService;

    @Value("${video.input.directory}")
    private String videoInputDirectory;

    public static void main(String[] args) {
        SpringApplication.run(StreamingServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        videoTranscoderService.transcodeToHLS(videoInputDirectory);
        System.out.println("Video transcoding completed.");
    }
}
