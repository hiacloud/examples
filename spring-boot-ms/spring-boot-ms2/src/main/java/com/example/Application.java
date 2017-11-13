package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableOAuth2Client
public class Application{
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
