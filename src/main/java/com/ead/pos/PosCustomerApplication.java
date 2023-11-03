package com.ead.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class PosCustomerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PosCustomerApplication.class, args);
	}

}
