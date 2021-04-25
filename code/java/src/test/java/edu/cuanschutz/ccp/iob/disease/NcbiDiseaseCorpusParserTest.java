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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public class NcbiDiseaseCorpusParserTest {

	private static final String EXPECTED_TEXT_1 = "A common human skin tumour is caused by activating mutations in beta-catenin."
			+ "\n"
			+ "WNT signalling orchestrates a number of developmental programs. In response to this stimulus, cytoplasmic beta-catenin (encoded by CTNNB1) is stabilized, enabling downstream transcriptional activation by members of the LEF/TCF family. One of the target genes for beta-catenin/TCF encodes c-MYC, explaining why constitutive activation of the WNT pathway can lead to cancer, particularly in the colon. Most colon cancers arise from mutations in the gene encoding adenomatous polyposis coli (APC), a protein required for ubiquitin-mediated degradation of beta-catenin, but a small percentage of colon and some other cancers harbour beta-catenin-stabilizing mutations. Recently, we discovered that transgenic mice expressing an activated beta-catenin are predisposed to developing skin tumours resembling pilomatricomas. Given that the skin of these adult mice also exhibits signs of de novo hair-follicle morphogenesis, we wondered whether human pilomatricomas might originate from hair matrix cells and whether they might possess beta-catenin-stabilizing mutations. Here, we explore the cell origin and aetiology of this common human skin tumour. We found nuclear LEF-1 in the dividing tumour cells, providing biochemical evidence that pilomatricomas are derived from hair matrix cells. At least 75% of these tumours possess mutations affecting the amino-terminal segment, normally involved in phosphorylation-dependent, ubiquitin-mediated degradation of the protein. This percentage of CTNNB1 mutations is greater than in all other human tumours examined thus far, and directly implicates beta-catenin/LEF misregulation as the major cause of hair matrix cell tumorigenesis in humans..";
	private static final String EXPECTED_TEXT_2 = "HFE mutations analysis in 711 hemochromatosis probands: evidence for S65C implication in mild form of hemochromatosis."
			+ "\n"
			+ "Hereditary hemochromatosis (HH) is a common autosomal recessive genetic disorder of iron metabolism. The HFE candidate gene encoding an HLA class I-like protein involved in HH was identified in 1996. Two missense mutations have been described  C282Y, accounting for 80% to 90% of HH chromosomes, and H63D, which is associated with a milder form of the disease representing 40% to 70% of non-C282Y HH chromosomes. We report here on the analysis of C282Y, H63D, and the 193A-- > T substitution leading to the S65C missense substitution in a large series of probands and controls. The results confirm that the C282Y substitution was the main mutation involved in hemochromatosis, accounting for 85% of carrier chromosomes, whereas the H63D substitution represented 39% of the HH chromosomes that did not carry the C282Y mutation. In addition, our screening showed that the S65C substitution was significantly enriched in probands with at least one chromosome without an assigned mutation. This substitution accounted for 7. 8% of HH chromosomes that were neither C282Y nor H63D. This enrichment of S65C among HH chromosomes suggests that the S65C substitution is associated with the mild form of hemochromatosis.";

	@Test
	public void testParseSample() throws IOException {
		InputStream inputStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "corpus-sample.txt");

		TextDocument td = null;
		TextAnnotation firstAnnot = null;
		NcbiDiseaseCorpusParser parser = new NcbiDiseaseCorpusParser(inputStream);

		/* FIRST DOCUMENT */
		assertTrue(parser.hasNext());
		td = parser.next();
		assertNotNull(td);
		assertEquals(EXPECTED_TEXT_1, td.getText());
		assertEquals("there should be 14 annotations", 14, td.getAnnotations().size());

		/* <pre>10192393 15 26 skin tumour DiseaseClass D012878</pre> */
		firstAnnot = td.getAnnotations().get(0);
		assertEquals("10192393", firstAnnot.getDocumentID());
		assertEquals(15, firstAnnot.getAnnotationSpanStart());
		assertEquals(26, firstAnnot.getAnnotationSpanEnd());
		assertEquals("skin tumour", firstAnnot.getCoveredText());
		assertEquals(
				"should have a single annotation set id [0]; 0 is the set id for DiseaseClass. See NcbiDiseaseCorpusParser.DISEASE_CLASS_ANNOT_SET.",
				CollectionsUtil.createSet(0), firstAnnot.getAnnotationSetIDs());
		assertEquals("MESH:D012878", firstAnnot.getClassMention().getMentionName());

		/* SECOND DOCUMENT */
		assertTrue(parser.hasNext());
		td = null;
		td = parser.next();
		assertNotNull(td);
		assertEquals(EXPECTED_TEXT_2, td.getText());
		assertEquals("there should be 13 annotations", 13, td.getAnnotations().size());

		/* <pre>10194428 30 45 hemochromatosis Modifier D016399</pre> */
		firstAnnot = td.getAnnotations().get(0);
		assertEquals("10194428", firstAnnot.getDocumentID());
		assertEquals(30, firstAnnot.getAnnotationSpanStart());
		assertEquals(45, firstAnnot.getAnnotationSpanEnd());
		assertEquals("hemochromatosis", firstAnnot.getCoveredText());
		assertEquals(
				"should have a single annotation set id [2]; 2 is the set id for Modifier. See NcbiDiseaseCorpusParser.MODIFIER_ANNOT_SET.",
				CollectionsUtil.createSet(2), firstAnnot.getAnnotationSetIDs());
		assertEquals("MESH:D016399", firstAnnot.getClassMention().getMentionName());

		// there should only by 2 documents
		assertFalse(parser.hasNext());

	}

}
