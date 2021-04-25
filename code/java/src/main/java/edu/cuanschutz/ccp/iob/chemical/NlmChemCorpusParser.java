package edu.cuanschutz.ccp.iob.chemical;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.io.BioCDocumentReader;

import edu.ucdenver.ccp.common.string.StringUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.SpanUtils;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;

/**
 * Parses a directory of BioC files from the NLM-Chem corpus
 *
 */
public class NlmChemCorpusParser implements Iterator<TextDocument> {
	private Iterator<File> fileIterator;

	public NlmChemCorpusParser(File directory) throws IOException {
		fileIterator = Arrays.asList(directory.listFiles()).iterator();
	}

	@Override
	public boolean hasNext() {
		return fileIterator.hasNext();
	}

	@Override
	public TextDocument next() {
		if (!hasNext())
			throw new NoSuchElementException();

		File file = fileIterator.next();
		try {
			return parseCorpusFile(new FileInputStream(file));
		} catch (FactoryConfigurationError | XMLStreamException | IOException e) {
			throw new IllegalArgumentException("Unable to parse file: " + file.getAbsolutePath());
		}

	}

	public static TextDocument parseCorpusFile(InputStream inputStream)
			throws FactoryConfigurationError, XMLStreamException, IOException {

		List<TextDocument> docsToReturn = new ArrayList<TextDocument>();

		String text = "";
		try (BioCDocumentReader reader = new BioCDocumentReader(inputStream, getBioCXmlResolver())) {
			String source = reader.readCollectionInfo().getSource();
			BioCDocument doc = null;
			while ((doc = reader.readDocument()) != null) {
				String docId = source + doc.getID();
				TextAnnotationFactory taFactory = TextAnnotationFactory.createFactoryWithDefaults(docId);

				List<TextAnnotation> entityAnnotations = new ArrayList<TextAnnotation>();
				text = processBioCDocument(text, doc, entityAnnotations, taFactory);

				TextDocument td = new TextDocument(docId, source, text);
				td.addAnnotations(entityAnnotations);
				// check annotations for any that start with whitespace and adjust accordingly.
				adjustForAddedWhitespace(td);

				docsToReturn.add(td);

			}
		}

		if (docsToReturn.size() != 1) {
			throw new IllegalArgumentException(
					"Expected one document in the input, however observed " + docsToReturn.size());
		}

		return docsToReturn.get(0);
	}

	private static void adjustForAddedWhitespace(TextDocument td) {
		for (TextAnnotation ta : td.getAnnotations()) {
			String substring = td.getText().substring(ta.getAggregateSpan().getSpanStart(),
					ta.getAggregateSpan().getSpanEnd());

			while (StringUtil.startsWithRegex(substring, "\\s")) {
				Span span = ta.getAggregateSpan();
				int updatedStart = span.getSpanStart() + 1;
				int updatedEnd = (span.getSpanEnd() < td.getText().length()) ? span.getSpanEnd() + 1
						: span.getSpanEnd();

				Span updatedSpan = new Span(updatedStart, updatedEnd);
				ta.setSpan(updatedSpan);

				substring = td.getText().substring(ta.getAggregateSpan().getSpanStart(),
						ta.getAggregateSpan().getSpanEnd());
			}
		}
	}

	/**
	 * process each passage in a BioC document. Return the plain text of the
	 * document with passage byte offsets corresponding to those stipulated by the
	 * document. During processing, create section annotations for main sections,
	 * e.g. INTRO, RESULTS, etc., as well as for paragraphs, section headings, etc.
	 * 
	 * @param text
	 * @param doc
	 * @param sections
	 * @param openSections
	 * @param taFactory
	 * @return
	 */
	private static String processBioCDocument(String text, BioCDocument doc, List<TextAnnotation> annotations,
			TextAnnotationFactory taFactory) {
		for (BioCPassage passage : doc.getPassages()) {
			if (passage.getText().isPresent()) {

				int textOffset = text.length();
				text = updateText(text, doc, passage);

				for (BioCAnnotation biocAnnot : passage.getAnnotations()) {
					Optional<String> annotType = biocAnnot.getInfon("type");
					List<Span> spans = locationsToSpans(biocAnnot.getLocations(), passage.getOffset(), textOffset);
					String expectedCoveredText = biocAnnot.getText().get();
					String actualCoveredText = SpanUtils.getCoveredText(spans, text);

					if (!actualCoveredText.equals(expectedCoveredText)) {
						System.out.println("Expected != Actual: " + expectedCoveredText + " != " + actualCoveredText);
						// try shifting the span slightly to see if a match can be found
						for (int i = -5; i < 5; i++) {
//							System.out.println("before: " + spans.toString());
							List<Span> updatedSpans = offsetSpans(spans, i);
//							System.out.println("after: " + updatedSpans.toString());
							actualCoveredText = SpanUtils.getCoveredText(updatedSpans, text);
//							System.out.println("actual: " + actualCoveredText);
							if (actualCoveredText.equals(expectedCoveredText)) {
								spans = updatedSpans;
								break;
							}
						}

						if (!actualCoveredText.equals(expectedCoveredText)) {
							throw new IllegalStateException("Covered text does not match. " + "type: " + annotType
									+ " spans: " + spans.toString() + " expected-covered: " + expectedCoveredText
									+ " actual-covered: |" + SpanUtils.getCoveredText(spans, text) + "|");
						}
					}

					TextAnnotation ta = taFactory.createAnnotation(spans, text,
							new DefaultClassMention(annotType.get()));
					annotations.add(ta);
				}
			} else {
				// encountered passage with no text
			}
		}
		return text;
	}

	private static List<Span> offsetSpans(List<Span> spans, int offset) {
		List<Span> newSpans = new ArrayList<Span>();
		for (Span span : spans) {
			newSpans.add(new Span(span.getSpanStart() + offset, span.getSpanEnd() + offset));
		}
		return newSpans;
	}

	private static List<Span> locationsToSpans(Set<BioCLocation> locations, int passageOffset, int textOffset) {
		List<Span> spans = new ArrayList<Span>();

		for (BioCLocation location : locations) {
			int spanStart = location.getOffset() - passageOffset + textOffset + (passageOffset == 0 ? 0 : 1);
			int spanEnd = spanStart + location.getLength();
			spans.add(new Span(spanStart, spanEnd));
		}

		Collections.sort(spans, Span.ASCENDING());
		return spans;
	}

	/**
	 * Add a line break after each passage
	 * @param text
	 * @param doc
	 * @param passage
	 * @return
	 */
	private static String updateText(String text, BioCDocument doc, BioCPassage passage) {
		/* each passage is by default separated by a line break */
		text += (((!text.isEmpty()) ? "\n" : "") + passage.getText().get());
		return text;
	}

	/**
	 * This method thanks to:
	 * https://stackoverflow.com/questions/10685668/how-to-load-a-relative-system-dtd-into-a-stax-parser
	 * 
	 * @return
	 */
	private static XMLResolver getBioCXmlResolver() {
		return new XMLResolver() {

			@Override
			public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace)
					throws XMLStreamException {

				/*
				 * The systemID argument is the same dtd file specified in the xml file header.
				 * For example, if the xml header is <!DOCTYPE dblp SYSTEM "dblp.dtd">, then
				 * systemID will be "dblp.dtd".
				 * 
				 */
				return Thread.currentThread().getContextClassLoader().getResourceAsStream(systemID);

			}
		};
	}

	public static void main(String[] args) {

		File directory = new File("/Users/bill/Downloads/FINAL_v1/ALL");
		try {
			NlmChemCorpusParser parser = new NlmChemCorpusParser(directory);
			while (parser.hasNext()) {

				TextDocument td = parser.next();
				System.out.println(td.getSourceid() + " -- " + td.getAnnotations().size());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
