package com.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LostAndFoundBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(LostAndFoundBackendApplication.class, args);
	}

}
