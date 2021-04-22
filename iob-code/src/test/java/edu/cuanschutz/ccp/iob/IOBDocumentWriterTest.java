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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;

public class IOBDocumentWriterTest {

	// 0123456789012345678901234567890123456789012345678901234567890123456
	// 1 2 3 4 5 6
	private final String documentText = "The cow jumped over the moon. The quick brown fox was chasing it.";

	/* The/DT cow/NN jumped/VBD over/IN the/DT moon/NN ./. */
	/* The/DT quick/JJ brown/JJ fox/NN was/VBD chasing/VBG it/PRP ./. */

	@Test
	public void testRemoveNestedAnnotations() {
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();

		// cow
		TextAnnotation annot_cow = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)), documentText,
				new DefaultClassMention("CL:0000000"));
		// cow jumped
		TextAnnotation annot_cow_jumped = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 14)),
				documentText, new DefaultClassMention("CL:0000000"));
		// moon
		TextAnnotation annot_moon = factory.createAnnotation(CollectionsUtil.createList(new Span(24, 28)), documentText,
				new DefaultClassMention("CL:0000000"));

		List<TextAnnotation> annotations = new ArrayList<TextAnnotation>();
		annotations.add(annot_cow);
		annotations.add(annot_cow_jumped);
		annotations.add(annot_moon);

		List<TextAnnotation> updatedAnnotations = IOBDocumentWriter.removeNestedAnnotations(annotations);

		assertEquals(2, updatedAnnotations.size());

		assertEquals(annot_cow_jumped, updatedAnnotations.get(0));
		assertEquals(annot_moon, updatedAnnotations.get(1));

	}

	@Test
	public void testRemoveNestedAnnotations_discontinuous() {
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();

		// cow
		TextAnnotation annot_cow = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)), documentText,
				new DefaultClassMention("CL:0000000"));
		// cow jumped
		TextAnnotation annot_cow_over = factory.createAnnotation(
				CollectionsUtil.createList(new Span(4, 7), new Span(15, 19)), documentText,
				new DefaultClassMention("CL:0000000"));
		// moon
		TextAnnotation annot_moon = factory.createAnnotation(CollectionsUtil.createList(new Span(24, 28)), documentText,
				new DefaultClassMention("CL:0000000"));

		List<TextAnnotation> annotations = new ArrayList<TextAnnotation>();
		annotations.add(annot_cow);
		annotations.add(annot_cow_over);
		annotations.add(annot_moon);

		List<TextAnnotation> updatedAnnotations = IOBDocumentWriter.removeNestedAnnotations(annotations);

		assertEquals(2, updatedAnnotations.size());

		assertEquals(annot_cow_over, updatedAnnotations.get(0));
		assertEquals(annot_moon, updatedAnnotations.get(1));

	}

//	/**
//	 * this is now abstract so should be tested when implemented
//	 */
//	@Test
//	public void testGetConceptAnnotations() {
//
//		List<TextAnnotation> annotList = new ArrayList<TextAnnotation>();
//		List<TextAnnotation> expectedOutputAnnotList = new ArrayList<TextAnnotation>();
//
//		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
//
//		TextAnnotation conceptAnnot1 = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)),
//				documentText, new DefaultClassMention("CL:0000000"));
//		TextAnnotation conceptAnnot2 = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)),
//				documentText, new DefaultClassMention("NCBITaxon:0000000"));
//		TextAnnotation conceptAnnot3 = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)),
//				documentText, new DefaultClassMention("CL_EXT:some_cell_class"));
//		TextAnnotation tokenAnnot = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)), documentText,
//				new DefaultClassMention("DT"));
//
//		annotList.add(conceptAnnot1);
//		annotList.add(conceptAnnot2);
//		annotList.add(conceptAnnot3);
//		annotList.add(tokenAnnot);
//
//		expectedOutputAnnotList.add(conceptAnnot1);
//		expectedOutputAnnotList.add(conceptAnnot2);
//		expectedOutputAnnotList.add(conceptAnnot3);
//
//		
//		List<TextAnnotation> conceptAnnotations = IOBDocumentWriter.getConceptAnnotations(annotList);
//
//		assertEquals(expectedOutputAnnotList, conceptAnnotations);
//
////		
////		 List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
////				List<TextAnnotation> conceptAnnotations = new ArrayList<TextAnnotation>();
////
////				Set<String> annotTypes = new HashSet<String>();
////
////				for (TextAnnotation ta : annotations) {
////					String type = ta.getClassMention().getMentionName();
////					if (type.matches("[A-Za-z]+:\\d+") || type.contains("_EXT")) {
////						conceptAnnotations.add(ta);
////						annotTypes.add(type);
////					}
////				}
////
////				// for (String type : annotTypes) {
////				// System.out.println(type);
////				// }
////
////				return conceptAnnotations;
////			}
//
//		
//		
//	}

}
