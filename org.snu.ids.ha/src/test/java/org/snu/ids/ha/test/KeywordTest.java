package org.snu.ids.ha.test;


import java.util.List;

import junit.framework.TestCase;

import org.snu.ids.ha.core.Keyword;
import org.snu.ids.ha.core.MAnalyzerFull;
import org.snu.ids.ha.core.MAnalyzerLite;
import org.snu.ids.ha.core.MAnalyzerMini;
import org.snu.ids.ha.core.MProcessor;
import org.snu.ids.ha.core.Sentence;
import org.snu.ids.ha.util.Timer;


public class KeywordTest
	extends TestCase
{
	String	str	= null;


	public void setUp()
	{
		str = "이들 법인들은 대부분 민법 제32조, 공익법인설립·운영에 관한 법률 제4조, 사회복지사업법 제16조에 따라 설립되었다.";
		str = "김씨가";
		str = "그 땐 단지 ‘IMF라는 것이 나쁜거구나’ 라고만 생각했지 그것이 정확히 무엇인지 몰랐습니다.";
		str = "정거장에서 사건이 생기면, 그것은 이 소설의 배경이지만, 정거장이기 때문에 사건이 생기면, 그 정거장은 이 소설의 모티브가 되는 것이다.";
		str = "내가 만일 안철수라면";
	}


	public void testKEFull()
	{
		testKE(new MAnalyzerFull());
	}


	public void testKELite()
	{
		testKE(new MAnalyzerLite());
	}


	public void testKEMini()
	{
		testKE(new MAnalyzerMini());
	}


	public void testKE(MAnalyzerFull ma)
	{
		System.out.println("====KEYWORD :: " + ma.getClass().getSimpleName());
		MProcessor mp = new MProcessor();

		Timer timer = new Timer();
		timer.start();
		try {
			List<Sentence> analRet = mp.divide(ma.analyze(str));
			for( Sentence sent : analRet ) {
				System.out.println(sent.getSentence());
				System.out.println(sent);
				List<Keyword> keywords = sent.getKeywords(2);
				for( Keyword keyword : keywords ) {
					System.out.println("\t" + keyword);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		timer.stop();
		timer.printMsg("Anal time");
		System.out.println();
		System.out.println();
	}
}
