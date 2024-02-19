package com.s8.demoservice;

import com.s8.demoservice.controller.CustomerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DemoServiceApplicationTests {
	@Autowired
	CustomerController customerController;

	@Test
	void contextLoads() {
		assertThat(customerController).isNotNull();
	}

}
