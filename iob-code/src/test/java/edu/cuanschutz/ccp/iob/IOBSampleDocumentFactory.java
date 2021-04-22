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

import java.util.ArrayList;
import java.util.List;

import edu.ucdenver.ccp.common.collections.CollectionsUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.Span;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotationFactory;
import edu.ucdenver.ccp.nlp.core.mention.impl.DefaultClassMention;

public class IOBSampleDocumentFactory {

	// 0123456789012345678901234567890123456789012345678901234567890123456
	// 1 2 3 4 5 6
	private static final String documentText = "The cow jumped over the moon. The quick brown fox was chasing it.";

	/* The/DT cow/NN jumped/VBD over/IN the/DT moon/NN ./. */
	/* The/DT quick/JJ brown/JJ fox/NN was/VBD chasing/VBG it/PRP ./. */

	public static TextDocument getSampleDocument() {
		TextAnnotationFactory factory = TextAnnotationFactory.createFactoryWithDefaults();
		List<TextAnnotation> annotations = new ArrayList<TextAnnotation>();

		TextAnnotation annot_the = factory.createAnnotation(CollectionsUtil.createList(new Span(0, 3)), documentText,
				new DefaultClassMention("DT"));
		TextAnnotation annot_cow = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)), documentText,
				new DefaultClassMention("NN"));
		TextAnnotation annot_jumped = factory.createAnnotation(CollectionsUtil.createList(new Span(8, 14)),
				documentText, new DefaultClassMention("VBD"));
		TextAnnotation annot_over = factory.createAnnotation(CollectionsUtil.createList(new Span(15, 19)), documentText,
				new DefaultClassMention("IN"));
		TextAnnotation annot_the2 = factory.createAnnotation(CollectionsUtil.createList(new Span(20, 23)), documentText,
				new DefaultClassMention("DT"));
		TextAnnotation annot_moon = factory.createAnnotation(CollectionsUtil.createList(new Span(24, 28)), documentText,
				new DefaultClassMention("NN"));
		TextAnnotation annot_period = factory.createAnnotation(CollectionsUtil.createList(new Span(28, 29)),
				documentText, new DefaultClassMention("."));
		TextAnnotation annot_sentence1 = factory.createAnnotation(CollectionsUtil.createList(new Span(0, 29)),
				documentText, new DefaultClassMention("sentence"));
		TextAnnotation annot_the3 = factory.createAnnotation(CollectionsUtil.createList(new Span(30, 33)), documentText,
				new DefaultClassMention("DT"));
		TextAnnotation annot_quick = factory.createAnnotation(CollectionsUtil.createList(new Span(34, 39)),
				documentText, new DefaultClassMention("JJ"));
		TextAnnotation annot_brown = factory.createAnnotation(CollectionsUtil.createList(new Span(40, 45)),
				documentText, new DefaultClassMention("JJ"));
		TextAnnotation annot_fox = factory.createAnnotation(CollectionsUtil.createList(new Span(46, 49)), documentText,
				new DefaultClassMention("NN"));
		TextAnnotation annot_was = factory.createAnnotation(CollectionsUtil.createList(new Span(50, 53)), documentText,
				new DefaultClassMention("VBD"));
		TextAnnotation annot_chasing = factory.createAnnotation(CollectionsUtil.createList(new Span(54, 61)),
				documentText, new DefaultClassMention("VBG"));
		TextAnnotation annot_it = factory.createAnnotation(CollectionsUtil.createList(new Span(62, 64)), documentText,
				new DefaultClassMention("PRP"));
		TextAnnotation annot_period2 = factory.createAnnotation(CollectionsUtil.createList(new Span(64, 65)),
				documentText, new DefaultClassMention("."));
		TextAnnotation annot_sentence2 = factory.createAnnotation(CollectionsUtil.createList(new Span(30, 65)),
				documentText, new DefaultClassMention("sentence"));

		// cow
		TextAnnotation concept_annot_cow = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 7)),
				documentText, new DefaultClassMention("CL:0000000"));
		// cow jumped
		TextAnnotation concept_annot_cow_jumped = factory.createAnnotation(CollectionsUtil.createList(new Span(4, 14)),
				documentText, new DefaultClassMention("CL:0000000"));
		// moon
		TextAnnotation concept_annot_moon = factory.createAnnotation(CollectionsUtil.createList(new Span(24, 28)),
				documentText, new DefaultClassMention("CL:0000001"));

		// quick brown fox
		TextAnnotation concept_annot_quick_brown_fox = factory.createAnnotation(
				CollectionsUtil.createList(new Span(34, 49)), documentText, new DefaultClassMention("CL:0000002"));

		annotations.add(annot_the);
		annotations.add(annot_cow);
		annotations.add(annot_jumped);
		annotations.add(annot_over);
		annotations.add(annot_the2);
		annotations.add(annot_moon);
		annotations.add(annot_period);
		annotations.add(annot_sentence1);
		annotations.add(annot_the3);
		annotations.add(annot_quick);
		annotations.add(annot_brown);
		annotations.add(annot_fox);
		annotations.add(annot_was);
		annotations.add(annot_chasing);
		annotations.add(annot_it);
		annotations.add(annot_period2);
		annotations.add(annot_sentence2);
		annotations.add(concept_annot_cow);
		annotations.add(concept_annot_cow_jumped);
		annotations.add(concept_annot_moon);
		annotations.add(concept_annot_quick_brown_fox);

		TextDocument td = new TextDocument("12345", "PMC", documentText);
		td.setAnnotations(annotations);
		return td;

	}

}
