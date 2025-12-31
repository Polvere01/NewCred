package br.com.newcred;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@EnableScheduling
@SpringBootApplication
public class NewcredApplication {

	public static void main(String[] args) {

//        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
//        System.out.println(enc.encode("@Macaredonda123"));
        SpringApplication.run(NewcredApplication.class, args);
	}

}
