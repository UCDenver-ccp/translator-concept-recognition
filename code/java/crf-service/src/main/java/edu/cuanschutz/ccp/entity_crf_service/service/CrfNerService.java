package edu.cuanschutz.ccp.entity_crf_service.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import edu.cuanschutz.ccp.entity_crf_service.CRFUtil;
import edu.cuanschutz.ccp.entity_crf_service.CRFUtil.Offset;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

/**
 *
 *
 */
@Service
public class CrfNerService implements NerService {

	private static final Logger logger = Logger.getLogger(CrfNerService.class.getName());

	private CRFUtil crfUtil;

	public void init(InputStream modelStream) {
		try {
			logger.log(Level.INFO, "Loading CRF model...");
			crfUtil = new CRFUtil(modelStream);
		} catch (ClassCastException | ClassNotFoundException | InstantiationException | IOException e) {
			throw new IllegalArgumentException("Unable to load CRF model.", e);
		}
	}

	@Override
	public List<TextAnnotation> extractEntities(List<TextAnnotation> sentenceAnnots) {

		List<TextAnnotation> entityAnnots = new ArrayList<TextAnnotation>();

		for (TextAnnotation sentenceAnnot : sentenceAnnots) {
			List<TextAnnotation> conceptAnnots = crfUtil.classifySentence(sentenceAnnot, Offset.IN_DOCUMENT);
			entityAnnots.addAll(conceptAnnots);
		}

		return entityAnnots;

	}

}
