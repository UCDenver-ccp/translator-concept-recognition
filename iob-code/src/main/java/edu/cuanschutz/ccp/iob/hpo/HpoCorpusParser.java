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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import edu.stanford.nlp.io.IOUtils;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.reader.Line;
import edu.ucdenver.ccp.common.file.reader.StreamLineIterator;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;

public class HpoCorpusParser implements Iterator<TextDocument> {
	private static final CharacterEncoding DEFAULT_ENCODING = CharacterEncoding.UTF_8;

	private final Iterator<File> fileIter;

	private File annotationFileDirectory;

	private CharacterEncoding encoding;

	private Map<String, Map<String, String>> corpusFixMap;

	public HpoCorpusParser(File textFileDirectory, File annotationFileDirectory, CharacterEncoding encoding)
			throws FileNotFoundException, IOException {
		this.annotationFileDirectory = annotationFileDirectory;
		this.encoding = encoding;

		populateCorpusFixMap();

		File[] files = textFileDirectory.listFiles();
		fileIter = Arrays.asList(files).iterator();

	}

	/**
	 * There are some span errors in the data that are documented in
	 * src/main/resources/edu/cuanschutz/ccp/iob/hpo/hpo-corpus-fixes.tsv. This
	 * method loads a map containing the fixes so that they can be applied as the
	 * corpus is parsed. Loads the corpusFixMap (key = document ID, value = Map from
	 * original annotation string to fixed annotation string)
	 * 
	 * @throws IOException
	 */
	private void populateCorpusFixMap() throws IOException {
		corpusFixMap = new HashMap<String, Map<String, String>>();

		for (StreamLineIterator lineIter = new StreamLineIterator(
				ClassPathUtil.getResourceStreamFromClasspath(getClass(), "hpo-corpus-fixes.txt"),
				CharacterEncoding.UTF_8, null); lineIter.hasNext();) {
			String[] cols = lineIter.next().getText().split("###");
			String docId = cols[0];
			String origAnnotLine = cols[1];
			String fixedAnnotLine = cols[2];
			if (corpusFixMap.containsKey(docId)) {
				corpusFixMap.get(docId).put(origAnnotLine, fixedAnnotLine);
			} else {
				Map<String, String> map = new HashMap<String, String>();
				map.put(origAnnotLine, fixedAnnotLine);
				corpusFixMap.put(docId, map);
			}
		}
	}

	public HpoCorpusParser(File annotationFileDirectory, File textFileDirectory)
			throws FileNotFoundException, IOException {
		this(annotationFileDirectory, textFileDirectory, DEFAULT_ENCODING);
	}

	@Override
	public boolean hasNext() {
		return fileIter.hasNext();
	}

	@Override
	public TextDocument next() {
		if (!hasNext())
			throw new NoSuchElementException();

		File textFile = fileIter.next();
		File annotationFile = new File(annotationFileDirectory, textFile.getName());

		System.out.println("txt: " + textFile.getAbsolutePath());
		System.out.println("annot: " + annotationFile.getAbsolutePath());

		try {
			return parseDocument(textFile, annotationFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse the lines in the input buffer and return the corresponding
	 * {@link TextDocument}. First line is the title. Second line is the abstract.
	 * Other lines are stand-off annotations.
	 * 
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	private TextDocument parseDocument(File textFile, File annotationFile) throws IOException {
		String docText = IOUtils.stringFromFile(textFile.getAbsolutePath(), encoding.getCharacterSetName());
		String docId = textFile.getName();
		TextAnnotationFactory annotFactory = TextAnnotationFactory.createFactoryWithDefaults(docId);
		List<TextAnnotation> annots = new ArrayList<TextAnnotation>();
		for (StreamLineIterator lineIter = new StreamLineIterator(annotationFile, encoding); lineIter.hasNext();) {
			Line line = lineIter.next();
			String text = fixCorpusAnnotationLine(docId, line.getText());
			annots.add(parseAnnotation(text, annotFactory));
		}

		TextDocument td = new TextDocument(docId, "PubMed", docText.toString());

		validateAnnotationCoveredText(td.getText(), annots);

		td.addAnnotations(annots);
		return td;
	}

	/**
	 * There are some annotations in the data that have incorrect spans. Fixes to
	 * these errors have been loaded into the corpusFixMap from
	 * src/main/resources/edu/cuanschutz/ccp/iob/hpo/hpo-corpus-fixes.tsv. This
	 * method swaps the original annotation line for the fixed line when
	 * appropriate.
	 * 
	 * @param parseAnnotation
	 * @return
	 */
	private String fixCorpusAnnotationLine(String docId, String annotationLine) {
		if (corpusFixMap.containsKey(docId)) {
			if (corpusFixMap.get(docId).containsKey(annotationLine)) {
				return corpusFixMap.get(docId).get(annotationLine);
			}
		}
		return annotationLine;
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

			if (!coveredText.equalsIgnoreCase(expectedCoveredText)) {
				throw new IllegalStateException(String.format(
						"The annotation covered text '%s' does not match the expected text in the document '%s' [%d, %d]. Annotation spans must be incorrect.",
						coveredText, expectedCoveredText, spanStart, spanEnd));
			}
		}

	}

	/**
	 * Parse the stand-off annotation format and return an {@link TextAnnotation}
	 * object. Example:
	 * 
	 * <pre>
	 * [14::27]        HP_0001156 | brachydactyly
	 * </pre>
	 *
	 * @param line
	 * @param annotFactory
	 * @return
	 */
	private TextAnnotation parseAnnotation(String line, TextAnnotationFactory annotFactory) {
		String[] cols = line.split("\\t");
		int index = 0;
		String spanStr = cols[index++];
		String[] classAndText = cols[index++].split(" \\| ");

		spanStr = spanStr.substring(1, spanStr.length() - 1);
		String[] spanValues = spanStr.split("::");
		int spanStart = Integer.parseInt(spanValues[0]);
		int spanEnd = Integer.parseInt(spanValues[1]);

		String coveredText = classAndText[1];
		String hpoId = classAndText[0].replace("_", ":");

		TextAnnotation annot = annotFactory.createAnnotation(spanStart, spanEnd, coveredText, hpoId);

		return annot;
	}

}
