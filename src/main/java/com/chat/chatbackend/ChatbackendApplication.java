package com.chat.chatbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableCassandraRepositories
public class ChatbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatbackendApplication.class, args);
	}

}
