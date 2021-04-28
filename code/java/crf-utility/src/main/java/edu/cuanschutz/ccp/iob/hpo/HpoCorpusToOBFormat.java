package edu.cuanschutz.ccp.iob.hpo;

/*-
 * #%L
 * Text Mining Provider concept recognition module
 * %%
 * Copyright (C) 2021 Regents of the University of Colorado
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.cuanschutz.ccp.iob.IOBDocumentWriter;
import edu.cuanschutz.ccp.iob.IOBDocumentWriter.Format;
import edu.cuanschutz.ccp.iob.StanfordIOBDocumentWriter;
import edu.cuanschutz.ccp.iob.StanfordOBDocumentWriter;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.Annotator;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.annotation.impl.DefaultTextAnnotation;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

/**
 * create a version of the corpus suitable to train the Stanford CRF model to
 * detect disease entities.
 *
 */
public class HpoCorpusToOBFormat {

	static List<TextAnnotation> myFilterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
		List<TextAnnotation> sentenceAndTokenAnnots = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annotations) {
			String type = annot.getClassMention().getMentionName();
			if (!(type.startsWith("HP"))) {
				sentenceAndTokenAnnots.add(annot);
			}
		}
		return sentenceAndTokenAnnots;
	}

	static List<TextAnnotation> myGetConceptAnnotations(List<TextAnnotation> annotations) {
		List<TextAnnotation> conceptAnnotations = new ArrayList<TextAnnotation>();

		Set<String> annotTypes = new HashSet<String>();

		for (TextAnnotation ta : annotations) {
			String type = ta.getClassMention().getMentionName();
			if (type.startsWith("HP")) {
				conceptAnnotations.add(ta);
				annotTypes.add(type);
			}
		}

		// for (String type : annotTypes) {
		// System.out.println(type);
		// }

		return conceptAnnotations;
	}

	public static void convertCorpus(File textFileDirectory, File annotationFileDirectory, Format format,
			SentenceDetector sentenceDetector, Tokenizer simpleTokenizer, File outputDir)
			throws FileNotFoundException, IOException {

		IOBDocumentWriter docWriter = null;
		switch (format) {
		case IOB:
			docWriter = new StanfordIOBDocumentWriter() {

				@Override
				protected List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
					return myFilterSentenceAndTokenAnnots(annotations);
				}

				@Override
				protected List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
					return myGetConceptAnnotations(annotations);
				}
			};
			break;
		case OB:
			docWriter = new StanfordOBDocumentWriter() {

				@Override
				protected List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
					return myFilterSentenceAndTokenAnnots(annotations);
				}

				@Override
				protected List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
					return myGetConceptAnnotations(annotations);
				}
			};
			break;
		default:
			throw new IllegalArgumentException("Format " + format.name() + " is not supported.");
		}

		for (HpoCorpusParser parser = new HpoCorpusParser(textFileDirectory, annotationFileDirectory); parser
				.hasNext();) {
			TextDocument td = parser.next();

			File outputFile = new File(outputDir, td.getSourceid() + ".ob");

			// document contains the disease mention annotations -- need to add tokens and
			// sentences

			// get sentences then tokenize each sentence
			List<TextAnnotation> sentences = getSentenceAnnots(sentenceDetector, td.getText());
			List<TextAnnotation> tokens = extractTokenAnnots(simpleTokenizer, sentences);

			td.addAnnotations(sentences);
			td.addAnnotations(tokens);

			docWriter.serialize(td, outputFile, CharacterEncoding.UTF_8);

		}

	}

	private static List<TextAnnotation> splitSentencesOnLineBreaks(List<TextAnnotation> annots) {
		/*
		 * divide any sentences with line breaks into multiple sentences, splitting at
		 * the line breaks
		 */
		List<TextAnnotation> toKeep = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annots) {
			String coveredText = annot.getCoveredText();
			if (coveredText.contains("\n")) {
				String[] sentences = coveredText.split("\\n");
				int index = annot.getAnnotationSpanStart();
				for (String s : sentences) {
					if (!s.isEmpty()) {
						TextAnnotation sentAnnot = createSentenceAnnot(index, index + s.length(), s);
						index = index + s.length() + 1;
						toKeep.add(sentAnnot);
					} else {
						index++;
					}
				}
				// validate - span end of more recently added sentence should be equal to the
				// span end of the original annot
				int originalSpanEnd = annot.getAnnotationSpanEnd();
				int end = toKeep.get(toKeep.size() - 1).getAnnotationSpanEnd();
				assert end == originalSpanEnd;
			} else {
				toKeep.add(annot);
			}
		}
		return toKeep;
	}

	private static TextAnnotation createSentenceAnnot(int spanStart, int spanEnd, String coveredText) {
		DefaultTextAnnotation annot = new DefaultTextAnnotation(spanStart, spanEnd);
		annot.setCoveredText(coveredText);
		DefaultClassMention cm = new DefaultClassMention("sentence");
		annot.setClassMention(cm);
		annot.setAnnotator(new Annotator(null, "OpenNLP", "OpenNLP"));
		return annot;
	}

	private static List<TextAnnotation> extractTokenAnnots(Tokenizer tokenizer, List<TextAnnotation> sentences) {
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		List<TextAnnotation> tokenAnnots = new ArrayList<TextAnnotation>();
		for (TextAnnotation sentence : sentences) {
			int documentOffset = sentence.getAnnotationSpanStart();
			Span[] tokenSpans = tokenizer.tokenizePos(sentence.getCoveredText());

			for (Span tokenSpan : tokenSpans) {
				TextAnnotation tokenAnnot = factory.createAnnotation(tokenSpan.getStart() + documentOffset,
						tokenSpan.getEnd() + documentOffset,
						tokenSpan.getCoveredText(sentence.getCoveredText()).toString(), "token");
				tokenAnnots.add(tokenAnnot);
			}
		}
		return tokenAnnots;
	}

	private static List<TextAnnotation> getSentenceAnnots(SentenceDetector sentenceDetector, String plainText) {
		List<TextAnnotation> annots = new ArrayList<TextAnnotation>();
		Span[] spans = sentenceDetector.sentPosDetect(plainText);
		for (Span span : spans) {
			TextAnnotation annot = createSentenceAnnot(span.getStart(), span.getEnd(),
					span.getCoveredText(plainText).toString());
			annots.add(annot);
		}

		List<TextAnnotation> sentences = splitSentencesOnLineBreaks(annots);
		return sentences;
	}

	private static SentenceDetectorME initializeSentenceDetector() throws IOException {
		InputStream modelStream = ClassPathUtil.getResourceStreamFromClasspath(HpoCorpusToOBFormat.class,
				"/de/tudarmstadt/ukp/dkpro/core/opennlp/lib/sentence-en-maxent.bin");
		SentenceModel model = new SentenceModel(modelStream);
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);
		return sentenceDetector;
	}

	public static void main(String[] args) {
		try {
			File baseDir = new File(args[0]);

			SentenceDetectorME sentenceDetector;
			sentenceDetector = initializeSentenceDetector();

			Tokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;

			File textFileDirectory = new File(baseDir, "train/text");
			File annotationFileDirectory = new File(baseDir, "train/annotations");
			File outputDir = new File(baseDir, "train/ob");
			outputDir.mkdirs();
			convertCorpus(textFileDirectory, annotationFileDirectory, Format.OB, sentenceDetector, simpleTokenizer,
					outputDir);

			textFileDirectory = new File(baseDir, "test/text");
			annotationFileDirectory = new File(baseDir, "test/annotations");
			outputDir = new File(baseDir, "test/ob");
			outputDir.mkdirs();
			convertCorpus(textFileDirectory, annotationFileDirectory, Format.OB, sentenceDetector, simpleTokenizer,
					outputDir);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
