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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.AnnotationSet;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

public class NcbiDiseaseCorpusParser implements Iterator<TextDocument> {
	private static final CharacterEncoding DEFAULT_ENCODING = CharacterEncoding.UTF_8;

	/*
	 * There are 4 possible annotation categories in the NCBI Disease Corpus. We
	 * distinguish them using {@link AnnotationSet} assignment to each annotation.
	 */

	private final static AnnotationSet DISEASE_CLASS_ANNOT_SET = new AnnotationSet(0, "DiseaseClass", "");
	private final static AnnotationSet SPECIFIC_DISEASE_ANNOT_SET = new AnnotationSet(1, "SpecificDisease", "");
	private final static AnnotationSet MODIFIER_ANNOT_SET = new AnnotationSet(2, "Modifier", "");
	private final static AnnotationSet COMPOSITE_MENTION_ANNOT_SET = new AnnotationSet(3, "CompositeMention", "");

	private StreamLineIterator lineIter;
	private List<String> buffer;

	public NcbiDiseaseCorpusParser(File corpusFile, CharacterEncoding encoding)
			throws FileNotFoundException, IOException {
		this(new FileInputStream(corpusFile), encoding);
	}

	public NcbiDiseaseCorpusParser(File corpusFile) throws FileNotFoundException, IOException {
		this(corpusFile, DEFAULT_ENCODING);
	}

	public NcbiDiseaseCorpusParser(InputStream corpusStream) throws IOException {
		this(corpusStream, DEFAULT_ENCODING);
	}

	public NcbiDiseaseCorpusParser(InputStream corpusStream, CharacterEncoding encoding) throws IOException {
		lineIter = new StreamLineIterator(corpusStream, encoding, null);
		advanceBuffer();
	}

	/**
	 * fill the buffer with the lines that correspond to the next document
	 */
	private void advanceBuffer() {
		buffer = new ArrayList<String>();
		while (lineIter.hasNext()) {
			Line line = lineIter.next();
			String lineText = line.getText();
			if (lineText.trim().isEmpty() && buffer.isEmpty()) {
				// skip any leading blank lines
				continue;
			} else if (!lineText.isEmpty()) {
				buffer.add(lineText);
			} else {
				// empty line indicates the end of a document
				break;
			}
		}

		if (buffer.isEmpty()) {
			// then there are no more documents to return
			buffer = null;
		}

	}

	@Override
	public boolean hasNext() {
		return buffer != null && !buffer.isEmpty();
	}

	@Override
	public TextDocument next() {
		if (!hasNext())
			throw new NoSuchElementException();

		TextDocument doc = parseDocumentFromBuffer(buffer);
		advanceBuffer();
		return doc;

	}

	/**
	 * Parse the lines in the input buffer and return the corresponding
	 * {@link TextDocument}. First line is the title. Second line is the abstract.
	 * Other lines are stand-off annotations.
	 * 
	 * @param buffer
	 * @return
	 */
	private TextDocument parseDocumentFromBuffer(List<String> buffer) {
		StringBuffer docText = new StringBuffer();
		String docId = null;
		List<TextAnnotation> annots = new ArrayList<TextAnnotation>();

		TextAnnotationFactory annotFactory = null;

		Pattern p = Pattern.compile("^(\\d+)\\|([ta])\\|(.*)$");
		for (String line : buffer) {
			Matcher m = p.matcher(line);
			if (m.find()) {
				docId = m.group(1);
				annotFactory = TextAnnotationFactory.createFactoryWithDefaults(docId);
				String spacer = (docText.length() == 0) ? "" : "\n";
				docText.append(spacer + m.group(3).trim());
			} else {
				annots.add(parseAnnotation(line, annotFactory));
			}
		}

		TextDocument td = new TextDocument(docId, "PubMed", docText.toString());

		validateAnnotationCoveredText(td.getText(), annots);

		td.addAnnotations(annots);
		return td;
	}

	/**
	 * Ensure annotation covered text and spans match what is expected in the
	 * document text
	 * 
	 * @param text
	 * @param annots
	 */
	private void validateAnnotationCoveredText(String documentText, List<TextAnnotation> annots) {
		for (TextAnnotation annot : annots) {
			int spanStart = annot.getAnnotationSpanStart();
			int spanEnd = annot.getAnnotationSpanEnd();
			String coveredText = annot.getCoveredText();

			String expectedCoveredText = documentText.substring(spanStart, spanEnd);

			if (!coveredText.equals(expectedCoveredText)) {
				System.err.println(String.format(
						"The annotation covered text '%s' does not match the expected text in the document '%s'. Annotation spans must be incorrect.",
						coveredText, expectedCoveredText));
//				throw new IllegalStateException(String.format(
//						"The annotation covered text '%s' does not match the expected text in the document '%s'. Annotation spans must be incorrect.",
//						coveredText, expectedCoveredText));
			}
		}

	}

	/**
	 * Parse the stand-off annotation format and return an {@link TextAnnotation}
	 * object. Example:
	 * 
	 * <pre>
	 * 10192393        15      26      skin tumour     DiseaseClass    D012878
	 * </pre>
	 *
	 * @param line
	 * @param annotFactory
	 * @return
	 */
	private TextAnnotation parseAnnotation(String line, TextAnnotationFactory annotFactory) {
		String[] cols = line.split("\\t");
		int index = 0;
		@SuppressWarnings("unused")
		String docId = cols[index++];
		int spanStart = Integer.parseInt(cols[index++]);
		int spanEnd = Integer.parseInt(cols[index++]);
		String coveredText = cols[index++];
		String annotationCategory = cols[index++];
		String meshOrOmimId = cols[index++];
		if (!meshOrOmimId.startsWith("OMIM:")) {
			meshOrOmimId = "MESH:" + meshOrOmimId;
		}

		TextAnnotation annot = annotFactory.createAnnotation(spanStart, spanEnd, coveredText, meshOrOmimId);
		annot.setAnnotationSets(CollectionsUtil.createSet(getAnnotationSet(annotationCategory)));

		return annot;

	}

	private AnnotationSet getAnnotationSet(String annotationCategory) {
		switch (annotationCategory) {
		case "DiseaseClass":
			return DISEASE_CLASS_ANNOT_SET;
		case "SpecificDisease":
			return SPECIFIC_DISEASE_ANNOT_SET;
		case "Modifier":
			return MODIFIER_ANNOT_SET;
		case "CompositeMention":
			return COMPOSITE_MENTION_ANNOT_SET;

		default:
			throw new IllegalArgumentException(
					String.format("Unhandled annotation category: %s. Code change required.", annotationCategory));
		}
	}

}
