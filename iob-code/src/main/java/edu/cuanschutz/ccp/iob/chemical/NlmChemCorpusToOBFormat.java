package edu.cuanschutz.ccp.iob.chemical;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.cuanschutz.ccp.iob.CorpusToOBFormat;
import edu.cuanschutz.ccp.iob.IOBDocumentWriter.Format;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

public class NlmChemCorpusToOBFormat extends CorpusToOBFormat {

	public NlmChemCorpusToOBFormat(Iterator<TextDocument> corpusParser) {
		super(corpusParser);
	}

	public static void main(String[] args) {
		try {
			File nlmChemCorpusDirectory = new File(args[0]);
			File outputDir = new File(args[1]);
			Format format = Format.valueOf(args[2]);

//			File nlmChemCorpusDirectory = new File("/Users/bill/Downloads/FINAL_v1/ALL");
//			File outputDir = new File("/Users/bill/Downloads/FINAL_v1/ob");

			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}

			SentenceDetectorME sentenceDetector;
			sentenceDetector = initializeSentenceDetector();

			Tokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;

			NlmChemCorpusParser corpusParser = new NlmChemCorpusParser(nlmChemCorpusDirectory);
			NlmChemCorpusToOBFormat converter = new NlmChemCorpusToOBFormat(corpusParser);
			converter.convertCorpus(format, sentenceDetector, simpleTokenizer, outputDir);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	@Override
	protected List<TextAnnotation> myFilterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
		List<TextAnnotation> sentenceAndTokenAnnots = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annotations) {
			String type = annot.getClassMention().getMentionName();
			if (!(type.startsWith("Chemical"))) {
				sentenceAndTokenAnnots.add(annot);
			}
		}
		return sentenceAndTokenAnnots;
	}

	@Override
	protected List<TextAnnotation> myGetConceptAnnotations(List<TextAnnotation> annotations) {
		List<TextAnnotation> conceptAnnotations = new ArrayList<TextAnnotation>();

		Set<String> annotTypes = new HashSet<String>();

		for (TextAnnotation ta : annotations) {
			String type = ta.getClassMention().getMentionName();
			if (type.startsWith("Chemical")) {
				conceptAnnotations.add(ta);
				annotTypes.add(type);
			}
		}

		return conceptAnnotations;
	}

}
