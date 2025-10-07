package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@ComponentScan
@SpringBootApplication
@EnableMongoAuditing
public class NoteKeepApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteKeepApplication.class, args);
    }
}
//test