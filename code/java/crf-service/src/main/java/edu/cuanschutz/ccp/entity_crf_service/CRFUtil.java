package edu.cuanschutz.ccp.entity_crf_service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.SerializationUtils;

import com.google.common.annotations.VisibleForTesting;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

public class CRFUtil implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Offset {
		/**
		 * return character offsets relative to the sentence
		 */
		IN_SENTENCE,
		/**
		 * return character offsets relative to the document
		 */
		IN_DOCUMENT
	}

	private final AbstractSequenceClassifier<CoreLabel> classifier;

	public CRFUtil(File modelFile) throws ClassCastException, ClassNotFoundException, FileNotFoundException,
			IOException, InstantiationException {
		this(new GZIPInputStream(new FileInputStream(modelFile)));
	}

	public CRFUtil(InputStream modelStream)
			throws ClassCastException, ClassNotFoundException, IOException, InstantiationException {
		classifier = CRFClassifier.getClassifier(modelStream);
	}

	public List<TextAnnotation> classifySentence(TextAnnotation sentenceAnnot) {
		return classifySentence(sentenceAnnot, Offset.IN_SENTENCE);
	}

	public List<TextAnnotation> classifySentence(TextAnnotation sentenceAnnot, Offset offset) {
		String sentence = sentenceAnnot.getCoveredText();
		List<Triple<String, Integer, Integer>> tripleList = classifier.classifyToCharacterOffsets(sentence);

		List<TextAnnotation> annots = createAnnotations(sentenceAnnot, offset, tripleList);

		return annots;

	}

	@VisibleForTesting
	protected static List<TextAnnotation> createAnnotations(TextAnnotation sentenceAnnot, Offset offset,
			List<Triple<String, Integer, Integer>> tripleList) {
		List<TextAnnotation> annots = new ArrayList<TextAnnotation>();
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults(sentenceAnnot.getDocumentID());
		for (Triple<String, Integer, Integer> triple : tripleList) {
			/*
			 * if offset == Offset.IN_DOCUMENT then the spans must be offset with the offset
			 * of the sentence within the document
			 */
			int spanStart = (offset == Offset.IN_DOCUMENT) ? triple.second() + sentenceAnnot.getAnnotationSpanStart()
					: triple.second();
			int spanEnd = (offset == Offset.IN_DOCUMENT) ? triple.third() + sentenceAnnot.getAnnotationSpanStart()
					: triple.third();
			TextAnnotation annot = factory.createAnnotation(spanStart, spanEnd,
					sentenceAnnot.getCoveredText().substring(triple.second(), triple.third()), triple.first());
			annots.add(annot);
		}
		return annots;
	}

}
