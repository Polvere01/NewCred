package br.com.newcred;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NewcredApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewcredApplication.class, args);
	}

}
