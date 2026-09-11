package com.foodapp.foodorderingapp;

import com.foodapp.foodorderingapp.entity.Role;
import com.foodapp.foodorderingapp.repository.RoleJpaRepository;

import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.WebApplicationInitializer;

@SpringBootApplication
public class FoodOrderingAppApplication extends SpringBootServletInitializer  {	

	
	public static void main(String[] args) {
		SpringApplication.run(FoodOrderingAppApplication.class, args);
		System.out.println("Application ready");
	}
	@Bean
	public CommandLineRunner initialData(RoleJpaRepository roleJpaRepository){
		return args ->{
			if(roleJpaRepository.findAll().isEmpty()) {
				Role role = new Role();
				role.setName("ROLE_USER");
				roleJpaRepository.save(role);

				Role adminRole = new Role();
				adminRole.setName("ROLE_ADMIN");
				roleJpaRepository.save(adminRole);

				Role sellerRole = new Role();
				sellerRole.setName("ROLE_SELLER");
				roleJpaRepository.save(sellerRole);
			}
		};
	}
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(FoodOrderingAppApplication.class);
	}

	private static SpringApplicationBuilder customizerBuilder(SpringApplicationBuilder builder) {
		return builder.sources(FoodOrderingAppApplication.class).bannerMode(Banner.Mode.OFF);
	}
}
