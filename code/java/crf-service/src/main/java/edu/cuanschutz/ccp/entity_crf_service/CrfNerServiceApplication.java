package edu.cuanschutz.ccp.entity_crf_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import edu.cuanschutz.ccp.entity_crf_service.properties.ModelProperties;

@SpringBootApplication
@EnableConfigurationProperties({ ModelProperties.class })
public class CrfNerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrfNerServiceApplication.class, args);
	}

}
