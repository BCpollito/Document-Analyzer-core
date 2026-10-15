package com.portfolio.documentanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;


@SpringBootApplication()
public class DocumentanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentanalyzerApplication.class, args);
	}

}
