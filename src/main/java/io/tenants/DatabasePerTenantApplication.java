package io.tenants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DatabasePerTenantApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatabasePerTenantApplication.class, args);
	}

}
