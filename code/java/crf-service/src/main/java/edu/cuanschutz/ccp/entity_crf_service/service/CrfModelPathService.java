package edu.cuanschutz.ccp.entity_crf_service.service;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cuanschutz.ccp.entity_crf_service.properties.ModelProperties;
import lombok.Getter;

@Service
public class CrfModelPathService {

	/**
	 * The path to the CRF model to use
	 */
	@Getter
	private final Path modelPath;

	@Autowired
	public CrfModelPathService(ModelProperties modelProperties) {
		this.modelPath = Paths.get(modelProperties.getModelFile()).toAbsolutePath().normalize();
	}

}