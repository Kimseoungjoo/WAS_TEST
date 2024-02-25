package com.smwas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.smwas.comm.CommApi;
import com.smwas.session.SessionManager;
import com.smwas.tr.TranFile;
import com.smwas.util.LOGCAT;

@SpringBootApplication
@EnableScheduling
public class TestSmwasApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestSmwasApplication.class, args);
//		CommApi.getInstance().connectToServer();
		SessionManager.getInstance().initializedSession();
	}
}
