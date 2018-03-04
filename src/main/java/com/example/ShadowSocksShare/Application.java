package com.example.ShadowSocksShare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
// @EnableCaching
public class Application {

	// java -jar -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true -Dspring.profiles.active=dev ShadowSocks-Share-0.0.1-SNAPSHOT.jar --server.port=8080
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/**
	 * 设置默认时区
	 */
	@PostConstruct
	void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
	}
}
