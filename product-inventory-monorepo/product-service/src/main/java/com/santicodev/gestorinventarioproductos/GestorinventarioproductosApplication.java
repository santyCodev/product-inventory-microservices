package com.santicodev.gestorinventarioproductos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // Habilita el soporte de caching de Spring
public class GestorinventarioproductosApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestorinventarioproductosApplication.class, args);
	}
}
