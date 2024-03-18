package com.smwas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.smwas.session.SessionManager;

@SpringBootApplication
@EnableScheduling
public class TestSmwasApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestSmwasApplication.class, args);
		SessionManager.getInstance().initializedSession();
	}
}
