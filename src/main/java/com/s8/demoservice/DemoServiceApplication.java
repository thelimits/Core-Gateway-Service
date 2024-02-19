package com.s8.demoservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
public class DemoServiceApplication {
	@Autowired
	private AppConfiguration configuration;

	public static void main(String[] args) {
		SpringApplication.run(DemoServiceApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		((HttpComponentsClientHttpRequestFactory) requestFactory).setConnectTimeout(configuration.getTIMEOUT());
		((HttpComponentsClientHttpRequestFactory) requestFactory).setReadTimeout(configuration.getTIMEOUT());

		return builder.requestFactory(() -> requestFactory).build();

	}

}
