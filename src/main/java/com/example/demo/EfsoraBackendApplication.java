package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.example.demo", "com.efsora.envanter"})
public class EfsoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfsoraBackendApplication.class, args);
	}

}
