package edu.cuanschutz.ccp.iob;

/*-
 * #%L
 * Colorado Computational Pharmacology's file conversion
 * 						project
 * %%
 * Copyright (C) 2019 - 2020 Regents of the University of Colorado
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileComparisonUtil;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.ColumnOrder;
import edu.ucdenver.ccp.common.file.FileComparisonUtil.LineOrder;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public class StanfordIOBDocumentWriterTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testSerializeAnnotations() throws IOException {
		TextDocument td = IOBSampleDocumentFactory.getSampleDocument();

		CharacterEncoding encoding = CharacterEncoding.UTF_8;
		StanfordIOBDocumentWriter writer = new StanfordIOBDocumentWriter() {

			@Override
			protected List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
				List<TextAnnotation> sentenceAndTokenAnnots = new ArrayList<TextAnnotation>();
				for (TextAnnotation annot : annotations) {
					String type = annot.getClassMention().getMentionName();
					if (!(type.matches("[A-Za-z]+:\\d+") || type.contains("_EXT") || type.startsWith("NCBITaxon:")
							|| type.startsWith("PR:"))) {
						// startsWith NCBITaxon: to account for NCBITaxon:species and a few others.
						// startsWith PR: to account for e.g. PR:Q03019
						sentenceAndTokenAnnots.add(annot);
					}
				}
				return sentenceAndTokenAnnots;

			}

			@Override
			protected List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
				List<TextAnnotation> conceptAnnotations = new ArrayList<TextAnnotation>();

				Set<String> annotTypes = new HashSet<String>();

				for (TextAnnotation ta : annotations) {
					String type = ta.getClassMention().getMentionName();
					if (type.matches("[A-Za-z]+:\\d+") || type.contains("_EXT")) {
						conceptAnnotations.add(ta);
						annotTypes.add(type);
					}
				}

				// for (String type : annotTypes) {
				// System.out.println(type);
				// }

				return conceptAnnotations;

			}
		};
		File file = folder.newFile("annot.out");
		writer.serialize(td, file, encoding);

		/* @formatter:off */
		List<String> expectedLines = CollectionsUtil.createList("The\tO", "cow\tB", "jumped\tI", "over\tO", "the\tO",
				"moon\tB", ".\tO", "", "The\tO", "quick\tB", "brown\tI", "fox\tI", "was\tO", "chasing\tO", "it\tO",
				".\tO", "");
		/* @formatter:on */

		assertTrue(FileComparisonUtil.hasExpectedLines(file, encoding, expectedLines, "\\t", LineOrder.AS_IN_FILE,
				ColumnOrder.AS_IN_FILE));

	}

}
