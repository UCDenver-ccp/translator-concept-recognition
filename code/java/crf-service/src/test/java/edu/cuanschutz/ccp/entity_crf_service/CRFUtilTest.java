package edu.cuanschutz.ccp.entity_crf_service;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.cuanschutz.ccp.entity_crf_service.CRFUtil.Offset;
import edu.stanford.nlp.util.Triple;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

public class CRFUtilTest {

	@Test
	public void testCreateAnnotations() {

		String sentence1 = "Excitatory neurons on average suppressed other neurons and had a centre-surround influence profile over anatomical space.";
		String sentence2 = "A neuron's influence on its neighbour depended on their similarity in activity.";
		String sentence3 = "Notably, neurons suppressed activity in similarly tuned neurons more than in dissimilarly tuned neurons.";

		String documentText = sentence1 + " " + sentence2 + " " + sentence3;

		int sent1Start = 0;
		int sent1End = sentence1.length();
		int sent2Start = sent1End + 1;
		int sent2End = sent2Start + sentence2.length();
		int sent3Start = sent2End + 1;
		int sent3End = sent3Start + sentence3.length();

		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		TextAnnotation sentAnnot3 = factory.createAnnotation(sent3Start, sent3End, sentence3, "sentence");

		int start1 = 9;
		int end1 = 16;
		int start2 = 56;
		int end2 = 63;
		int start3 = 96;
		int end3 = 103;

		/* entities extracted from the 3rd sentence */
		List<Triple<String, Integer, Integer>> crfOutputTriples = Arrays.asList(
				new Triple<String, Integer, Integer>("ENTITY", start1, end1),
				new Triple<String, Integer, Integer>("ENTITY", start2, end2),
				new Triple<String, Integer, Integer>("ENTITY", start3, end3));

		List<TextAnnotation> annots = CRFUtil.createAnnotations(sentAnnot3, Offset.IN_SENTENCE, crfOutputTriples);
		assertEquals("there should be 3 annotations", 3, annots.size());
		TextAnnotation annot1 = factory.createAnnotation(start1, end1, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", sentence3.substring(start1, end1));
		TextAnnotation annot2 = factory.createAnnotation(start2, end2, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", sentence3.substring(start2, end2));
		TextAnnotation annot3 = factory.createAnnotation(start3, end3, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", sentence3.substring(start3, end3));
		assertEquals("annotations in the lists should be the same", Arrays.asList(annot1, annot2, annot3), annots);

		/* now test with spans relative to the entire document */
		// offset relative to the document by adding the start span of the sentence
		start1 = start1 + sent3Start;
		end1 = end1 + sent3Start;
		start2 = start2 + sent3Start;
		end2 = end2 + sent3Start;
		start3 = start3 + sent3Start;
		end3 = end3 + sent3Start;

		annots = CRFUtil.createAnnotations(sentAnnot3, Offset.IN_DOCUMENT, crfOutputTriples);
		assertEquals("there should be 3 annotations", 3, annots.size());
		annot1 = factory.createAnnotation(start1, end1, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", documentText.substring(start1, end1));
		annot2 = factory.createAnnotation(start2, end2, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", documentText.substring(start2, end2));
		annot3 = factory.createAnnotation(start3, end3, "neurons", "ENTITY");
		assertEquals("covered text should be 'neurons'", "neurons", documentText.substring(start3, end3));
		assertEquals("annotation in the lists should be the same", Arrays.asList(annot1, annot2, annot3), annots);

	}

}
