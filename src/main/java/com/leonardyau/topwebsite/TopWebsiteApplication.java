package com.leonardyau.topwebsite;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.leonardyau.topwebsite.service.FileWatchManager;


@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class TopWebsiteApplication {

	@Autowired
	private FileWatchManager fm;
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	
	public static void main(String[] args) {
		SpringApplication.run(TopWebsiteApplication.class, args);
	}

	@PostConstruct
	public void startWatcher() {
		fm.start();
	}
	
}
