package org.snu.ids.ha.test;


import java.io.PrintWriter;
import java.util.List;

import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.core.MAnalyzerFull;
import org.snu.ids.ha.core.MAnalyzerLite;
import org.snu.ids.ha.core.MAnalyzerMini;
import org.snu.ids.ha.core.MCandidate;
import org.snu.ids.ha.util.Timer;


public class AccuracyBMT
{
	public static void main(String[] args)
	{
		String[] files = new String[] { "data/Sejong1000.xml" };
		test(files, new MAnalyzerFull());
		test(files, new MAnalyzerLite());
		test(files, new MAnalyzerMini());
	}


	public static void test(String[] files, MAnalyzerFull ma)
	{

		Timer timer = new Timer();
		ANSSentence ts = null;
		try {

			PrintWriter pw = new PrintWriter("sejong_" + ma.getClass().getSimpleName() + ".txt", "UTF-8");

			int ansCnt = 0;
			double sumMAAccuracy1 = 0d, sumMAAccuracy2 = 0d;
			long analTime = 0;

			for( int j = 0; j < files.length && j < 1; j++ ) {
				TestDocument td = new TestDocument();
				td.read(files[j]);
				timer.start();
				for( int i = 0; i < td.size(); i++ ) {
					ts = td.get(i);

					timer.start();
					List<MCandidate> list1 = ma.analyze(ts.getOrg());
					timer.stop();
					analTime += timer.getIntervalL();

					List<MCandidate> list2 = ma.analyze(ts.getSentence());
					ANSSentence analResult1 = getSentence(list1);
					ANSSentence analResult2 = getSentence(list2);

					TestResult tr1 = new TestResult(ts, analResult1);
					TestResult tr2 = new TestResult(ts, analResult2);

					ansCnt++;
					sumMAAccuracy1 += tr1.getAccuracy();
					sumMAAccuracy2 += tr2.getAccuracy();

					pw.println(String.format("%s\t%4d%4d%4d%8.3f", files[j], tr1.ansLen, tr1.candLen, tr1.editDist, tr1.getAccuracy()));
					pw.println(String.format("%s\t%4d%4d%4d%8.3f", files[j], tr2.ansLen, tr2.candLen, tr2.editDist, tr2.getAccuracy()));
					pw.println(ts.getOrg());
					pw.println(ts.getSentence());
					pw.println(ts.getUnspaced());
					pw.println(tr1.getAnsMrpListStr());
					pw.println(tr1.getCandMrpListStr());
					pw.println(tr2.getCandMrpListStr());
					pw.println();
					pw.println();

					if( ansCnt % 50 == 0 ) {
						System.out.println(ansCnt);
					}

				}
				System.out.println(analTime + " ms " + files[j]);
			}

			System.out.println(sumMAAccuracy1 / ansCnt);
			System.out.println(sumMAAccuracy2 / ansCnt);
			System.out.println();

			pw.close();
		} catch (Exception e) {
			System.err.println(ts.getOrg());
			e.printStackTrace();
		}
	}


	public static ANSSentence getSentence(List<MCandidate> analRet)
	{
		ANSSentence sentence = new ANSSentence();
		ANSEojeol eojeol = new ANSEojeol();
		char[] array = null;
		int start = -1;
		MCandidate prevMC = null;
		for( MCandidate mc : analRet ) {
			if( array == null ) array = mc.getArray();
			if( mc.isWithSpace() ) {
				eojeol.merge();
				eojeol.setExp(new String(array, start, prevMC.getStart() + prevMC.getLength() - start));
				sentence.add(eojeol);
				eojeol = new ANSEojeol();
				start = -1;
			}
			if( start == -1 ) start = mc.getStart();
			for( int i = 0, size = mc.size(); i < size; i++ ) {
				eojeol.add(new ANSMorp(new String(mc.getWordAt(i)), mc.getTagNumAt(i)));
			}
			prevMC = mc;
		}
		eojeol.merge();
		eojeol.setExp(new String(array, start, prevMC.getStart() + prevMC.getLength() - start));
		sentence.add(eojeol);

		// set sentence
		StringBuffer sb = null;
		for( ANSEojeol ej : sentence ) {
			if( sb == null ) {
				sb = new StringBuffer();
			} else {
				sb.append(" ");
			}
			sb.append(ej.getExp());
		}
		sentence.setSentence(sb.toString());

		return sentence;
	}
}

class TestResult
{
	int				ansLen		= 0;
	int				candLen		= 0;

	int[][]			table		= null;
	int				editDist	= 0;

	List<ANSMorp>	ansMrpList	= null;
	List<ANSMorp>	candMrpList	= null;


	public TestResult(ANSSentence answer, ANSSentence cand)
	{
		ansMrpList = answer.getSimpleResult();
		candMrpList = cand.getSimpleResult();

		ansLen = ansMrpList.size();
		candLen = candMrpList.size();

		table = new int[ansLen + 1][candLen + 1];

		for( int i = 0; i <= ansLen; i++ ) {
			table[i][0] = i;
		}

		for( int i = 0; i <= candLen; i++ ) {
			table[0][i] = i;
		}

		for( int i = 0; i < ansLen; i++ ) {
			for( int j = 0; j < candLen; j++ ) {
				int cost = 1;
				ANSMorp mpAns = ansMrpList.get(i);
				ANSMorp mpCand = candMrpList.get(j);
				// 동일한 형태소인지 확인
				if( getWeakStr(mpAns).equals(getWeakStr(mpCand)) ) cost = 0;
				int deletionCost = table[i][j + 1] + 1;
				int insertionCost = table[i + 1][j] + 1;
				int substitutionCost = table[i][j] + cost;
				table[i + 1][j + 1] = Math.min(substitutionCost, Math.min(deletionCost, insertionCost));
			}
		}

		editDist = table[ansLen][candLen];
	}


	public String getTable()
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < ansLen + 1; i++ ) {
			for( int j = 0; j < candLen + 1; j++ ) {
				sb.append(String.format("%4d", table[i][j]));
			}
			sb.append("\n");
		}
		return sb.toString();
	}


	public double getAccuracy()
	{
		return (double) (Math.max(candLen, ansLen) - editDist) / (double) Math.max(candLen, ansLen);
	}


	String getAnsMrpListStr()
	{
		return getMrpListStr(ansMrpList);
	}


	String getCandMrpListStr()
	{
		return getMrpListStr(candMrpList);
	}


	String getMrpListStr(List<ANSMorp> mrpList)
	{
		StringBuffer sb = new StringBuffer();

		for( int i = 0, size = mrpList.size(); i < size; i++ ) {
			ANSMorp mp = mrpList.get(i);
			if( i > 0 ) sb.append(", ");
			sb.append(getWeakStr(mp));
		}

		return sb.toString();
	}


	final static long getWeakTag(long tag)
	{
		if( ((POSTag.N | POSTag.UN | POSTag.XSN) & tag) > 0 ) {
			return POSTag.N;
		} else if( (POSTag.V & tag) > 0 ) {
			return POSTag.V;
		} else if( ((POSTag.MM) & tag) > 0 ) {
			return POSTag.MM;
		} else if( (POSTag.EP & tag) > 0 ) {
			return POSTag.EP;
		} else if( (POSTag.EM & tag) > 0 ) {
			return POSTag.EM;
		} else if( (POSTag.S & tag) > 0 ) {
			return POSTag.S;
		} else if( (POSTag.J & tag) > 0 ) {
			return POSTag.J;
		}
		return tag;
	}


	final static String getWeakStr(ANSMorp mp)
	{
		StringBuffer sb = new StringBuffer();
		// 았, 었, 였 통일
		if( POSTag.isTagOf(mp.tagNum, POSTag.EP) ) {
			if( mp.word.equals("았") | mp.word.equals("었") | mp.word.equals("였") ) {
				sb.append("았");
			} else {
				sb.append(normalize(mp.word));
			}
		}
		// 아, 어, 여 통일
		else if( mp.isTagOf(POSTag.EM) ) {
			if( mp.word.equals("아") | mp.word.equals("어") | mp.word.equals("여") ) {
				sb.append("아");
			} else if( mp.word.equals("아서") | mp.word.equals("어서") | mp.word.equals("여서") ) {
				sb.append("아서");
			} else {
				sb.append(normalize(mp.word));
			}
		} else {
			sb.append(normalize(mp.word));
		}
		sb.append("/");
		sb.append(POSTag.getTag(getWeakTag(mp.tagNum)));
		return sb.toString();
	}


	final static String normalize(String str)
	{
		str = str.replaceAll("ᆫ", "ㄴ");
		str = str.replaceAll("ᆯ", "ㄹ");
		str = str.replaceAll("ᄆ", "ㅁ");
		str = str.replaceAll("ᄇ", "ㅂ");
		return str;
	}
}