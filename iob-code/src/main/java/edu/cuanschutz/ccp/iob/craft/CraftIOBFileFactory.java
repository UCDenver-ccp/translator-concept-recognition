package edu.cuanschutz.ccp.iob.craft;

/*-
 * #%L
 * Colorado Computational Pharmacology's BioLink Text
 * 						Mining project
 * %%
 * Copyright (C) 2020 Regents of the University of Colorado
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import edu.cuanschutz.ccp.iob.IOBDocumentWriter;
import edu.cuanschutz.ccp.iob.IOBDocumentWriter.Format;
import edu.cuanschutz.ccp.iob.StanfordIOBDocumentWriter;
import edu.cuanschutz.ccp.iob.StanfordOBDocumentWriter;
import edu.ucdenver.ccp.common.file.CharacterEncoding;
import edu.ucdenver.ccp.common.file.FileReaderUtil;
import edu.ucdenver.ccp.common.file.FileUtil;
import edu.ucdenver.ccp.file.conversion.TextDocument;
import edu.ucdenver.ccp.file.conversion.knowtator.KnowtatorDocumentReader;
import edu.ucdenver.ccp.file.conversion.treebank.SentenceTokenOnlyTreebankDocumentReader;
import edu.ucdenver.ccp.nlp.core.annotation.TextAnnotation;

/**
 * Generates IOB files for the CRAFT concept annotations -- to be used to train
 * the Stanford CRFClassifier.
 */
public class CraftIOBFileFactory {

	static List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
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

	static List<TextAnnotation> getConceptAnnotationsOnly(List<TextAnnotation> annotations) {
		// remove all sentences and tokens
		Set<TextAnnotation> sentencesAndTokens = new HashSet<TextAnnotation>(filterSentenceAndTokenAnnots(annotations));
		List<TextAnnotation> toReturn = new ArrayList<TextAnnotation>();
		for (TextAnnotation annot : annotations) {
			if (!sentencesAndTokens.contains(annot)) {
				toReturn.add(annot);
			}
		}
		return toReturn;
	}

	public static void createCraftStanfordIOB(File craftBaseDirectory, File outputDirectory, Format format)
			throws IOException {
		CharacterEncoding encoding = CharacterEncoding.UTF_8;

		File inputDirectory = new File(craftBaseDirectory, "concept-annotation");
		File treebankDirectory = new File(craftBaseDirectory,
				String.format("structural-annotation%streebank%spenn", File.separator, File.separator));
		File textFileDirectory = new File(craftBaseDirectory, String.format("articles%stxt", File.separator));

		IOBDocumentWriter docWriter = null;
		switch (format) {
		case IOB:
			docWriter = new StanfordIOBDocumentWriter() {

				@Override
				protected List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
					return CraftIOBFileFactory.filterSentenceAndTokenAnnots(annotations);
				}

				@Override
				protected List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
					return CraftIOBFileFactory.getConceptAnnotationsOnly(annotations);
				}
			};
			break;
		case OB:
			docWriter = new StanfordOBDocumentWriter() {

				@Override
				protected List<TextAnnotation> filterSentenceAndTokenAnnots(List<TextAnnotation> annotations) {
					return CraftIOBFileFactory.filterSentenceAndTokenAnnots(annotations);
				}

				@Override
				protected List<TextAnnotation> getConceptAnnotations(List<TextAnnotation> annotations) {
					return CraftIOBFileFactory.getConceptAnnotationsOnly(annotations);
				}
			};
			break;
		default:
			throw new IllegalArgumentException("Format " + format.name() + " is not supported.");
		}

		for (Iterator<File> fileIterator = FileUtil.getFileIterator(inputDirectory, true); fileIterator.hasNext();) {
			File file = fileIterator.next();
			if (file.getName().contains(".knowtator.xml")) {
				String id = file.getName().substring(0, file.getName().indexOf("."));
				System.out.println("Processing: " + file.getAbsolutePath());
				String sourceDb = "PMC";
				File txtFile = new File(textFileDirectory, id + ".txt");
				File treebankFile = new File(treebankDirectory, id + ".tree");

				String relativePath = file.getParentFile().getAbsolutePath()
						.substring(file.getAbsolutePath().indexOf("concept-annotation") + 18);
				relativePath = relativePath.replace("knowtator", format.name().toLowerCase());
				File outputFile = new File(outputDirectory,
						relativePath + File.separator + id + "." + format.name().toLowerCase());
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}

				System.out.println("Process: " + outputFile.getAbsolutePath());

				KnowtatorDocumentReader knowtatorReader = new KnowtatorDocumentReader();
				TextDocument td = knowtatorReader.readDocument(id, sourceDb, file, txtFile, encoding);

				SentenceTokenOnlyTreebankDocumentReader dr = new SentenceTokenOnlyTreebankDocumentReader();
				TextDocument treebankDoc = dr.readDocument(id, sourceDb, treebankFile, txtFile, encoding);

				td.addAnnotations(treebankDoc.getAnnotations());

				docWriter.serialize(td, outputFile, encoding);
			}
		}
	}

	public static void separateIntoTrainDevTest(File craftBaseDirectory, File iobDirectory, File baseOutputDirectory,
			Format format) throws IOException {
		CharacterEncoding encoding = CharacterEncoding.UTF_8;

		File trainDirectory = new File(baseOutputDirectory, "train");
		File devDirectory = new File(baseOutputDirectory, "dev");
		File testDirectory = new File(baseOutputDirectory, "test");
		File idDirectory = new File(craftBaseDirectory, String.format("articles%sids", File.separator));

		Set<String> trainIds = new HashSet<String>(
				FileReaderUtil.loadLinesFromFile(new File(idDirectory, "craft-ids-train.txt"), encoding));
		Set<String> devIds = new HashSet<String>(
				FileReaderUtil.loadLinesFromFile(new File(idDirectory, "craft-ids-dev.txt"), encoding));
		Set<String> testIds = new HashSet<String>(
				FileReaderUtil.loadLinesFromFile(new File(idDirectory, "craft-ids-test.txt"), encoding));

		for (Iterator<File> fileIterator = FileUtil.getFileIterator(iobDirectory, true); fileIterator.hasNext();) {
			File file = fileIterator.next();
			if (file.getName().contains("." + format.name().toLowerCase())) {
				String id = file.getName().substring(0, file.getName().indexOf("."));
				String relativePath = file.getParentFile().getAbsolutePath();
				relativePath = relativePath.replace(iobDirectory.getAbsolutePath(), "");
				File outputDirectory = null;
				if (trainIds.contains(id)) {
					outputDirectory = trainDirectory;
				} else if (devIds.contains(id)) {
					outputDirectory = devDirectory;
				} else if (testIds.contains(id)) {
					outputDirectory = testDirectory;
				} else {
					throw new IllegalStateException("id was not found in a set.");
				}

				File outputFile = new File(outputDirectory,
						relativePath + File.separator + id + "." + format.name().toLowerCase());
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
				System.out.println("copying to file: " + outputFile.getAbsolutePath());
				IOUtils.copy(new FileInputStream(file), new FileOutputStream(outputFile));
			}
		}

	}

	/**
	 * @param args 0 - CRAFT base directory <br>
	 *             1 - IOB directory with files divided into train/dev/test
	 *             sub-directories<br>
	 *             2 - Output format (OB or IOB)
	 * 
	 */
	public static void main(String[] args) {
		File craftBaseDirectory = new File(args[0]);
		File craftIOBDirectory = new File(args[1]);
		Format format = Format.valueOf(args[2]);

		try {

			File intermediateIOBDirectory = new File(craftIOBDirectory, "all");
			createCraftStanfordIOB(craftBaseDirectory, intermediateIOBDirectory, format);
			separateIntoTrainDevTest(craftBaseDirectory, intermediateIOBDirectory, craftIOBDirectory, format);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

}
