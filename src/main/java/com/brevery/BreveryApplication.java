package com.brevery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude = {
        de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration.class,
        de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration.class
})
@EnableAsync
@EnableCaching
public class BreveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(BreveryApplication.class, args);
	}

}
