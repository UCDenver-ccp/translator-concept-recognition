package edu.cuanschutz.ccp.entity_crf_service.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * This code was modified from
 * https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example
 *
 */
@ConfigurationProperties(prefix = "file")
public class ModelProperties {
	@Getter
	@Setter
	private String modelFile;

}
