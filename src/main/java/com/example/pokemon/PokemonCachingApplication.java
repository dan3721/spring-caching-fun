package com.example.pokemon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PokemonCachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonCachingApplication.class, args);
    }
}
