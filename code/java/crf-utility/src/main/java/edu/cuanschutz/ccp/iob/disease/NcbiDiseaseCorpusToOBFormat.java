package edu.cuanschutz.ccp.iob.disease;

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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.cuanschutz.ccp.iob.CorpusToOBFormat;
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
public class NcbiDiseaseCorpusToOBFormat extends CorpusToOBFormat {

	public NcbiDiseaseCorpusToOBFormat(Iterator<TextDocument> corpusParser) {
		super(corpusParser);
	}

	@Override
	protected List<TextAnnotation> myFilterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
		List<TextAnnotation> sentenceAndTokenAnnots = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annotations) {
			String type = annot.getClassMention().getMentionName();
			if (!(type.startsWith("MESH") || type.startsWith("OMIM"))) {
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
			if (type.startsWith("MESH") || type.contains("OMIM")) {
				conceptAnnotations.add(ta);
				annotTypes.add(type);
			}
		}

		return conceptAnnotations;
	}

	public static void main(String[] args) {
		try {
			File corpusFile = new File(args[0]);
			File outputDir = new File(args[1]);

			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}

			SentenceDetectorME sentenceDetector;
			sentenceDetector = initializeSentenceDetector();

			Tokenizer simpleTokenizer = SimpleTokenizer.INSTANCE;

			NcbiDiseaseCorpusParser corpusParser = new NcbiDiseaseCorpusParser(corpusFile);
			NcbiDiseaseCorpusToOBFormat converter = new NcbiDiseaseCorpusToOBFormat(corpusParser);
			converter.convertCorpus(Format.OB, sentenceDetector, simpleTokenizer, outputDir);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
