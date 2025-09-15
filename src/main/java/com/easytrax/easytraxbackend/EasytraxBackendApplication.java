package com.easytrax.easytraxbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EasytraxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasytraxBackendApplication.class, args);
    }

}
