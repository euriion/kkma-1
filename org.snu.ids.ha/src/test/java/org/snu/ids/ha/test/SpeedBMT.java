package org.snu.ids.ha.test;


import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import junit.framework.TestCase;

import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.core.CharArray;
import org.snu.ids.ha.core.MAnalyzerFull;
import org.snu.ids.ha.core.MAnalyzerLite;
import org.snu.ids.ha.core.MAnalyzerMini;
import org.snu.ids.ha.core.MCandidate;
import org.snu.ids.ha.core.Token;
import org.snu.ids.ha.core.Tokenizer;


public class SpeedBMT
	extends TestCase
{

	public void testTokenizer()
		throws Exception
	{
		File dir = new File("data\\sample2");
		DirBufferedReader r = new DirBufferedReader(dir, "euc-kr");
		String line = null;

		long st = System.currentTimeMillis();
		int i = 0;
		long lap = st;

		PrintWriter pw1 = new PrintWriter("keywords_Token.txt", "utf-8");
		while( (line = r.readLine()) != null ) {

			line = line.trim();
			if( line.length() == 0 ) continue;

			try {
				List<Token> tl = Tokenizer.tokenize(new CharArray(line));
				for( Token tk : tl ) {
					pw1.print(new String(tk.getWord()) + "\t");
				}
				pw1.println();
			} catch (Exception e) {
				System.err.println(line);
				throw e;
			}

			if( i % 10000 == 0 ) {
				System.out.println("" + i + ".. " + Runtime.getRuntime().totalMemory() + ", " + (System.currentTimeMillis() - lap));
				lap = System.currentTimeMillis();
			}
			i++;

		}
		pw1.close();
		System.out.println(i + ", time = " + (System.currentTimeMillis() - st) + "ms");
	}


	public void testMini()
		throws Exception
	{
		test(new MAnalyzerMini());
	}


	public void testLite()
		throws Exception
	{
		test(new MAnalyzerLite());
	}


	public void testFull()
		throws Exception
	{
		test(new MAnalyzerFull());
	}


	public void test(MAnalyzerFull ma)
		throws Exception
	{
		File dir = new File("data\\sample2");
		DirBufferedReader r = new DirBufferedReader(dir, "euc-kr");
		String line = null;

		long st = System.currentTimeMillis();
		int i = 0;
		long lap = st;

		PrintWriter pw1 = new PrintWriter("keywords_" + ma.getClass().getSimpleName() + ".txt", "utf-8");
		while( (line = r.readLine()) != null ) {

			line = line.trim();
			if( line.length() == 0 ) continue;

			try {
				for( MCandidate mc : ma.analyze(line) ) {
					for( int j = 0, size = mc.size(); j < size; j++ ) {
						if( mc.isTagOfAt(j, POSTag.NN | POSTag.O) ) {
							pw1.print(mc.getStringAt(j) + " ");
						}

					}
				}
				pw1.println();
			} catch (Exception e) {
				System.err.println(line);
				throw e;
			}

			if( i % 1000 == 0 ) {
				System.out.println("" + i + ".. " + Runtime.getRuntime().totalMemory() + ", " + (System.currentTimeMillis() - lap));
				lap = System.currentTimeMillis();
			}
			i++;

		}
		pw1.close();
		System.out.println(i + ", time = " + (System.currentTimeMillis() - st) + "ms");
	}
}
