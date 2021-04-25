package edu.cuanschutz.ccp.iob.chemical;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import edu.ucdenver.ccp.common.io.ClassPathUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;

public class NlmChemCorpusParserTest {

	@Test
	public void testParseCorpusFile() throws FactoryConfigurationError, XMLStreamException, IOException {
		InputStream stream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "4773500_v1.xml");
		TextDocument td = NlmChemCorpusParser.parseCorpusFile(stream);
		assertEquals("PMC4773500", td.getSourceid());
	}

	
	@Test
	public void testParseCorpusFile2() throws FactoryConfigurationError, XMLStreamException, IOException {
		InputStream stream = ClassPathUtil.getResourceStreamFromClasspath(getClass(), "5600090_v1.xml");
		TextDocument td = NlmChemCorpusParser.parseCorpusFile(stream);
		assertEquals("PMC5600090", td.getSourceid());
	}
}
