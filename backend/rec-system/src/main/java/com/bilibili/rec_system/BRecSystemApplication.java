package com.bilibili.rec_system;

import com.bilibili.rec_system.service.network.MessageReceiverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@SpringBootApplication
public class BRecSystemApplication {
	
	@Autowired
	private MessageReceiverService messageReceiverService;

	public static void main(String[] args) {
		SpringApplication.run(BRecSystemApplication.class, args);
	}
	
	@PostConstruct
	public void startMessageReceiver() {
		messageReceiverService.startService();
	}
	
	@PreDestroy
	public void stopMessageReceiver() {
		messageReceiverService.stopService();
	}
}
