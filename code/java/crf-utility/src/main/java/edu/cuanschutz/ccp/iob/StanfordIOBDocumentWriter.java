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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.ucdenver.ccp.file.conversion.conllu.CoNLLUFileRecord;

public abstract class StanfordIOBDocumentWriter extends IOBDocumentWriter {

	@Override
	protected void writeIOBLines(List<CoNLLUFileRecord> records, BufferedWriter writer) throws IOException {
//		writer.write("-DOCSTART- -X- -X- O\n\n");
		for (CoNLLUFileRecord record : records) {
			if (record.getWordIndex() > 0) {
				Pattern p = Pattern.compile("IOB=([BIO])");
				Matcher m = p.matcher(record.getMiscellaneous());
				String iobCode = null;
				if (m.find()) {
					iobCode = m.group(1);
				} else {
					throw new IllegalStateException("No IOB code for token record: " + record.toString());
				}
//				// skip the two middle columns
//				if (!iobCode.equals("O")) {
//					iobCode = iobCode + "-MISC";
//				}
				writer.write(record.getForm() + "\t" + iobCode + "\n");
			} else {
				writer.write("\n");
			}
		}
	}

}
