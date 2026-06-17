package com.resumeiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ResumeiqApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResumeiqApplication.class, args);
	}

}
