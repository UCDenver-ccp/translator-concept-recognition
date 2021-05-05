package edu.cuanschutz.ccp.entity_crf_service.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.cuanschutz.ccp.entity_crf_service.payload.CrfNerResponse;
import edu.cuanschutz.ccp.entity_crf_service.service.CrfModelPathService;
import edu.cuanschutz.ccp.entity_crf_service.service.CrfNerService;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

/**
 * Note that this is designed to work with the CRAFT distribution directory
 * structure (versions >=3.1). See https://github.com/UCDenver-ccp/CRAFT.
 * 
 * The evaluation also assumes that the bionlp format files for all gold
 * standard concept annotations have been created and placed in a directory
 * called "bionlp" next to the respective "knowtator" directories, e.g.
 * CRAFT-4.0.1/concept-annotation/CL/CL+extensions/bionlp
 *
 */
@RestController
public class CrfNerController {

	public static final String CRF_POST_ENTRY = "/crf";

	private static final Logger logger = Logger.getLogger(CrfNerController.class.getName());

	@Autowired
	private CrfNerService crfService;

	@Autowired
	private CrfModelPathService crfModelPathService;

	@PostConstruct
	public void init() throws IOException {
		crfService.init(new GZIPInputStream(Files.newInputStream(crfModelPathService.getModelPath())));
	}

	/**
	 * @param sentencesToProcess this is bionlp formatted annotations but with an
	 *                           extra column at the beginning to indicate the
	 *                           document identifier
	 * @return
	 */
	@PostMapping(CRF_POST_ENTRY)
	public CrfNerResponse uploadSentences(@RequestBody String sentencesToProcess) {
		List<TextAnnotation> sentenceAnnotsToProcess = extractAnnotations(sentencesToProcess);
		return runCrf(sentenceAnnotsToProcess);
	}

	private List<TextAnnotation> extractAnnotations(String sentencesToProcess) {
		List<TextAnnotation> sentenceAnnots = new ArrayList<TextAnnotation>();

		for (String line : sentencesToProcess.split("\\n")) {
			String[] cols = line.split("\\t");
			String documentId = cols[0];
			String annotId = cols[1];
			String coveredText = cols[3];

			String[] typeSpan = cols[2].split(" ");
			String type = typeSpan[0];
			int spanStart = Integer.parseInt(typeSpan[1]);
			int spanEnd = Integer.parseInt(typeSpan[2]);

			TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults(documentId);
			TextAnnotation sentenceAnnot = factory.createAnnotation(spanStart, spanEnd, coveredText, type);
			sentenceAnnots.add(sentenceAnnot);
		}
		return sentenceAnnots;
	}

	/**
	 * Run the evaluation
	 * 
	 * @param ontologyKey
	 * @param boundaryMatchStrategy
	 * @param sourceIdToTestDocMap
	 * @return
	 */
	private CrfNerResponse runCrf(List<TextAnnotation> sentencesAnnots) {
		List<TextAnnotation> entityAnnots = crfService.extractEntities(sentencesAnnots);
		return new CrfNerResponse(entityAnnots);
	}

}
