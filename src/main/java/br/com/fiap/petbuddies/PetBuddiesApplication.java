package br.com.fiap.petbuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetBuddiesApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetBuddiesApplication.class, args);
    }
}
