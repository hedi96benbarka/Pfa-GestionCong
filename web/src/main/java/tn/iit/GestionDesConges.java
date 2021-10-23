package tn.iit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestionDesConges {
    public static void main(String[] args) {
        SpringApplication.run(GestionDesConges.class, args);
    }
}
