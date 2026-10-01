package com.portfolio.documentanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

//esto se debe cambiar despuess, es solo para que no tire error de conexion a la base de datos
// yaque aun no nos conectamos a ninguna base de datos
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DocumentanalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentanalyzerApplication.class, args);
	}

}
