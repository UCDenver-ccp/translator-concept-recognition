package edu.cuanschutz.ccp.iob;

/*-
 * #%L
 * Colorado Computational Pharmacology's file conversion
 * 						project
 * %%
 * Copyright (C) 2019 - 2020 Regents of the University of Colorado
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Regents of the University of Colorado nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.collections.CollectionsUtil.SortOrder;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileWriterUtil;
import edu.ucdenver.ccp.file.conversion.DocumentWriter;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.conllcoref2012.CoNLLCoref2012DocumentReader;
import edu.ucdenver.ccp.file.conversion.conllu.CoNLLUDocumentWriter;
import edu.ucdenver.ccp.file.conversion.conllu.CoNLLUFileRecord;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.SpanUtils;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public abstract class IOBDocumentWriter extends DocumentWriter {

	public enum Format {
		OB, IOB
	}

	@Override
	public void serialize(TextDocument td, OutputStream outputStream, CharacterEncoding encoding) throws IOException {

		List<TextAnnotation> annotations = trimAnnotations(td);

		/*
		 * TD assumed to contain sentence & token/pos annotations + single/multi-word
		 * base NP annotations linked into IDENT chains and APPOS relations
		 */

		List<TextAnnotation> sentenceAndTokenAnnots = filterSentenceAndTokenAnnots(annotations);
		if (sentenceAndTokenAnnots.size() == 0) {
			throw new IllegalArgumentException("No sentence/token annotations were detected in the input document. "
					+ "Unable to write the CoNLLCoref 2011/12 format without sentence and token annotations.");
		}

//		if (td.getSourceid().equals("10323740")) {
//			for (TextAnnotation annot : sentenceAndTokenAnnots) {
//				System.out.println("-=====" + annot.getClassMention().getMentionName() + " -- " + annot.getCoveredText());
//			}
////			System.exit(-1);
//		}

		/*
		 * The structure of the CoNLL Coref 2011/12 file format is similar to that of
		 * CoNLL-U. It lists tokens sequentially with line breaks at sentence
		 * boundaries. We can use logic in the CoNLL-U Document Writer to get the token
		 * ordering.
		 */
		List<CoNLLUFileRecord> records = CoNLLUDocumentWriter.generateRecords(sentenceAndTokenAnnots);

		Map<Span, CoNLLUFileRecord> spanToRecordMap = populateSpanToRecordMap(records);
		Map<Span, CoNLLUFileRecord> sortedSpanToRecordMap = CollectionsUtil.sortMapByKeys(spanToRecordMap,
				SortOrder.ASCENDING);

		// get concept annotations
		List<TextAnnotation> conceptAnnotations = getConceptAnnotations(annotations);

		// remove nested concept annotations, keep annotations with greatest spans
		conceptAnnotations = removeNestedAnnotations(conceptAnnotations);
		Collections.sort(conceptAnnotations, TextAnnotation.BY_SPAN());

		// for each concept annotation, mark each token record with B or I
		for (TextAnnotation ta : conceptAnnotations) {
			Span annotSpan = ta.getAggregateSpan();
			// System.out.println(
			// "========= CONCEPT: " + ta.getCoveredText() + " -- " + annotSpan.toString() +
			// "
			// ==========");
			boolean started = false;
			for (Entry<Span, CoNLLUFileRecord> entry : sortedSpanToRecordMap.entrySet()) {
				Span span = entry.getKey();
				// System.out.println(entry.getValue().getForm() + " -- " + span.toString());
				if (annotSpan.overlaps(span)) {
					String misc = entry.getValue().getMiscellaneous();
					if (!started) {
						misc += ";IOB=B";
						started = true;
					} else {
						misc += ";IOB=I";
					}
					entry.getValue().setMiscellaneous(misc);
				}
				// since the spans are sorted we can break the loop when the beginning of the
				// annotation has passed the end of a span.
				if (annotSpan.getSpanEnd() < span.getSpanStart()) {
					break;
				}
			}
		}

		// mark any unmarked token records with O
		for (CoNLLUFileRecord record : records) {
			String misc = record.getMiscellaneous();
			if (misc != null) {
				// it will be null if this is a record marking a sentence break
				Pattern p = Pattern.compile("IOB=[BI]");
				Matcher m = p.matcher(record.getMiscellaneous());
				if (!m.find()) {
					misc += ";IOB=O";
					record.setMiscellaneous(misc);
				}
			}
		}

		// output IOB format:

		/*
		 * The CoNLL-2003 shared task data files contain four columns separated by a
		 * single space. Each word has been put on a separate line and there is an empty
		 * line after each sentence. The first item on each line is a word, the second a
		 * part-of-speech (POS) tag, the third a syntactic chunk tag and the fourth the
		 * named entity tag.
		 * 
		 * U.N. NNP I-NP I-ORG
		 * 
		 */

		// this is the input format used by Gluon (FOR BERT)
		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputStream, encoding)) {
			writeIOBLines(records, writer);
		}

//		// this is the input format used by the depends-on-the-definition tutorial (FOR BERT)
//		long sentenceId = 0;
//		try (BufferedWriter writer = FileWriterUtil.initBufferedWriter(outputStream, encoding)) {
//			for (CoNLLUFileRecord record : records) {
//				if (record.getWordIndex() > 0) {
//					Pattern p = Pattern.compile("IOB=([BIO])");
//					Matcher m = p.matcher(record.getMiscellaneous());
//					String iobCode = null;
//					if (m.find()) {
//						iobCode = m.group(1);
//					} else {
//						throw new IllegalStateException("No IOB code for token record: " + record.toString());
//					}
//					// skip the two middle columns
//					writer.write("Sentence: " + sentenceId + "," + record.getForm() + ",_," + iobCode + "\n");
//				} else {
//					sentenceId++;
//				}
//			}
//		}

	}

	public static List<TextAnnotation> trimAnnotations(TextDocument td) throws IOException {
		Set<TextAnnotation> blankAnnots = new HashSet<TextAnnotation>();
		List<TextAnnotation> annotations = td.getAnnotations();
		for (TextAnnotation annot : annotations) {
			String coveredText = SpanUtils.getCoveredText(annot.getSpans(), td.getText());
			annot.setCoveredText(coveredText);
			if (annot.getCoveredText().trim().length() != annot.getCoveredText().length()) {
				if (annot.getCoveredText().trim().length() == 0) {
					blankAnnots.add(annot);
				} else {
					CoNLLCoref2012DocumentReader.trimAnnotation(td.getText(), annot);
				}
			}
		}

		/* remove whitespace annotations */
		for (TextAnnotation blankAnnot : blankAnnots) {
			annotations.remove(blankAnnot);
		}
		return annotations;
	}

	protected abstract void writeIOBLines(List<CoNLLUFileRecord> records, BufferedWriter writer) throws IOException;

	public static Map<Span, CoNLLUFileRecord> populateSpanToRecordMap(List<CoNLLUFileRecord> records) {

		Map<Span, CoNLLUFileRecord> spanToRecordMap = new HashMap<Span, CoNLLUFileRecord>();

		for (CoNLLUFileRecord record : records) {
			if (record.getWordIndex() < 0) {
				continue;
			}

			Pattern p = Pattern.compile("SPAN_([0-9]+)\\|([0-9]+)");
			Matcher m = p.matcher(record.getMiscellaneous());
			if (m.find()) {
				int spanStart = Integer.parseInt(m.group(1));
				int spanEnd = Integer.parseInt(m.group(2));
				Span span = new Span(spanStart, spanEnd);
				if (!spanToRecordMap.containsKey(span)) {
					spanToRecordMap.put(span, record);
				} else {
					System.out.println("RECORD: \n" + record.toString());
					System.out.println("STORED RECORD: \n" + spanToRecordMap.get(span).toString());

//					throw new IllegalStateException("Observed records with duplicate spans. " + span.toString() + " -- "
//							+ record.getForm() + " || " + spanToRecordMap.get(span).getForm());
					System.out.println("Observed records with duplicate spans. " + span.toString() + " -- "
							+ record.getForm() + " || " + spanToRecordMap.get(span).getForm());
				}

				if (record.getWordIndex() > -1 && !record.getMiscellaneous().matches("SPAN_\\d+\\|\\d+")) {
					throw new IllegalStateException(
							"misc doesn't match single span pattern: " + record.getMiscellaneous() + ";;;");
				}
			} else {
				throw new IllegalStateException(
						"No span indicator (e.g. 'SPAN_0|5') found in the record miscellaneous field.");
			}
		}
		return spanToRecordMap;
	}

	static List<TextAnnotation> removeNestedAnnotations(List<TextAnnotation> annotations) {
		Collections.sort(annotations, TextAnnotation.BY_SPAN());
		Stack<TextAnnotation> annotStack = new Stack<TextAnnotation>();

		for (TextAnnotation ta : annotations) {
			Span aggregateSpan = ta.getAggregateSpan();
			if (annotStack.isEmpty()) {
				annotStack.push(ta);
			} else if (aggregateSpan.overlaps(annotStack.peek().getAggregateSpan())) {
				if (aggregateSpan.containsSpan(annotStack.peek().getAggregateSpan())) {
					annotStack.pop();
					annotStack.add(ta);
				} else if (annotStack.peek().getAggregateSpan().containsSpan(aggregateSpan)) {
					// do nothing since the longer span is already in the stack
				} else {
					System.out.println("\nIgnoring overlapping but not nested annotation (CT1 is ignored)");
					System.out.println("CT1: " + aggregateSpan.toString() + " -- " + ta.getCoveredText());
					System.out.println("CT2: " + annotStack.peek().getAggregateSpan().toString() + " -- "
							+ annotStack.peek().getCoveredText());
					// throw new IllegalStateException(
					// "Should not be able to get here? Spans overlap but one doesn't encompass the
					// other.");
				}

			} else {
				annotStack.push(ta);
			}
		}

		return new ArrayList<TextAnnotation>(annotStack);
	}

	protected abstract List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations);

	protected abstract List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations);
}
