package com.example.shoeapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class ShoeAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoeAppApplication.class, args);
    }

    @GetMapping("/api/hello")
    public String hello() {
        return "Hello from Backend 👋";
    }
}
