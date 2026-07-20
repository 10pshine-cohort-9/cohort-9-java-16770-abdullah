package com.abdullahadil.contactmanagement;

import org.springframework.boot.SpringApplication;

public class TestContactManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(ContactManagementSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
