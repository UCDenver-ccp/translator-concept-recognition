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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

public class HpoCorpusParserTest {

	private static final String DOC_ID = "1003450";
	private static final String EXPECTED_TEXT = "A syndrome of brachydactyly (absence of some middle or distal phalanges), aplastic or hypoplastic nails, symphalangism (ankylois of proximal interphalangeal joints), synostosis of some carpal and tarsal bones, craniosynostosis, and dysplastic hip joints is reported in five members of an Italian family. It may represent a previously undescribed autosomal dominant trait.\n";

	private File txtDirectory;
	private File annotDirectory;
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		txtDirectory = folder.newFolder("txt");
		InputStream txtInputStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "sample-txt");
		OutputStream txtOutputStream = new FileOutputStream(new File(txtDirectory, DOC_ID));
		IOUtils.copy(txtInputStream, txtOutputStream);

		annotDirectory = folder.newFolder("annot");
		InputStream annotInputStream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "sample-annot");
		OutputStream annotOutputStream = new FileOutputStream(new File(annotDirectory, DOC_ID));
		IOUtils.copy(annotInputStream, annotOutputStream);

	}

	@Test
	public void testParseSample() throws IOException {

		TextDocument td = null;
		TextAnnotation firstAnnot = null;
		HpoCorpusParser parser = new HpoCorpusParser(txtDirectory, annotDirectory);

		/* FIRST DOCUMENT */
		assertTrue(parser.hasNext());
		td = parser.next();
		assertNotNull(td);
		assertEquals(EXPECTED_TEXT, td.getText());
		assertEquals("there should be 13 annotations", 13, td.getAnnotations().size());

		/* <pre>10192393 15 26 skin tumour DiseaseClass D012878</pre> */
		firstAnnot = td.getAnnotations().get(0);
		assertEquals(DOC_ID, firstAnnot.getDocumentID());
		assertEquals(14, firstAnnot.getAnnotationSpanStart());
		assertEquals(27, firstAnnot.getAnnotationSpanEnd());
		assertEquals("brachydactyly", firstAnnot.getCoveredText());
		assertEquals("HP:0001156", firstAnnot.getClassMention().getMentionName());

		// there should only by 1 document
		assertFalse(parser.hasNext());

	}

}
