package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.demo", "com.efsora.envanter"})
public class EfsoraBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EfsoraBackendApplication.class, args);
	}

}
