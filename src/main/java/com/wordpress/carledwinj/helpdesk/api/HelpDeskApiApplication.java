package com.wordpress.carledwinj.helpdesk.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wordpress.carledwinj.helpdesk.api.entity.User;
import com.wordpress.carledwinj.helpdesk.api.enums.ProfileEnum;
import com.wordpress.carledwinj.helpdesk.api.repository.UserRepository;

@SpringBootApplication
public class HelpDeskApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpDeskApiApplication.class, args);
	}
	
	@Bean
	CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return execute -> { 
			initUsers(userRepository, passwordEncoder);
		};
	}
	
	private void initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		
		User adminUser = new User("admin@helpdesk.com", passwordEncoder.encode("123456"), ProfileEnum.ROLE_ADMIN);
		
		User foundUser = userRepository.findByEmail(adminUser.getEmail());
		
		if(foundUser == null) {
			userRepository.save(adminUser);
		}
		
	}
}