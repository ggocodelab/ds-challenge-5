package com.ggocodelab.dscommerce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DsChallenge5Application implements CommandLineRunner{
	
	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(DsChallenge5Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("ENCODE = " + passwordEncoder.encode("123456"));
		
		boolean result = passwordEncoder
				.matches("1234567", "$2a$10$N7SkKCa3r17ga.i.dF9iy.BFUBL2n3b6Z1CWSZWi/qy7ABq/E6VpO");
		System.out.println("RESULTADO = " + result);
	}
}
