package edu.cuanschutz.ccp.entity_crf_service.service;

import java.util.List;

import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public interface NerService {

	public List<TextAnnotation>  extractEntities(List<TextAnnotation>  sentenceDocument);
}
