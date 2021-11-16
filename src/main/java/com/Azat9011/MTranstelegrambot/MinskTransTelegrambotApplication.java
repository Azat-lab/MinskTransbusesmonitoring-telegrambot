package com.Azat9011.MTranstelegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
@EnableScheduling
public class MinskTransTelegrambotApplication {
    public static void main(String[] args) {
        ApiContextInitializer.init();

        SpringApplication.run(MinskTransTelegrambotApplication.class, args);
    }
}
