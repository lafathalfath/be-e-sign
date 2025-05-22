package org.bh_foundation.e_sign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ESignApplication {

	public static void main(String[] args) {
		SpringApplication.run(ESignApplication.class, args);
	}

}
