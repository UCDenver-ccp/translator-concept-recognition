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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import edu.ucdenver.ccp.common.file.FileUtil;

public class SplitHpoCorpusTestTrain {

	public static void main(String[] args) {
		File corpusDir = new File(args[0]);
		File outputDir = new File(args[1]);

		File textDir = new File(corpusDir, "Text");
		File annotDir = new File(corpusDir, "Annotations");

		File[] textFiles = textDir.listFiles();

		File testOutputDir = new File(outputDir, "test");
		File testOutputTextDir = new File(testOutputDir, "text");
		File testOutputAnnotDir = new File(testOutputDir, "annotations");

		File trainOutputDir = new File(outputDir, "train");
		File trainOutputTextDir = new File(trainOutputDir, "text");
		File trainOutputAnnotDir = new File(trainOutputDir, "annotations");

		FileUtil.mkdir(testOutputTextDir);
		FileUtil.mkdir(testOutputAnnotDir);
		FileUtil.mkdir(trainOutputTextDir);
		FileUtil.mkdir(trainOutputAnnotDir);

		Random rand = new Random();

		Set<File> testFiles = new HashSet<File>();

		// select 30 random test files
		while (testFiles.size() < 30) {
			int index = rand.nextInt(textFiles.length);
			File randomFile = textFiles[index];
			testFiles.add(randomFile);
		}

		try {
			for (File textFile : textFiles) {
				String filename = textFile.getName();
				boolean isTestFile = testFiles.contains(textFile);
				File outputTextFile = new File(isTestFile ? testOutputTextDir : trainOutputTextDir, filename);
				copyFile(textFile, outputTextFile);
				File annotFile = new File(annotDir, filename);
				File outputAnnotFile = new File(isTestFile ? testOutputAnnotDir : trainOutputAnnotDir, filename);
				copyFile(annotFile, outputAnnotFile);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void copyFile(File textFile, File outputTextFile) throws IOException, FileNotFoundException {
		try (InputStream input = new FileInputStream(textFile);
				OutputStream output = new FileOutputStream(outputTextFile)) {
			IOUtils.copy(input, output);
		}
	}

}
