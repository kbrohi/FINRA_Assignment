package com.finra.assignment.fileuploader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Start point of the application
 */

@SpringBootApplication
@EnableScheduling
public class FileUploaderApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileUploaderApiApplication.class, args);
	}
}
