package org.snu.ids.ha.test;


import java.util.List;

import junit.framework.TestCase;

import org.snu.ids.ha.core.CharArray;
import org.snu.ids.ha.core.Keyword;
import org.snu.ids.ha.core.MAnalyzerFull;
import org.snu.ids.ha.core.MAnalyzerLite;
import org.snu.ids.ha.core.MAnalyzerMini;
import org.snu.ids.ha.core.MCandidate;
import org.snu.ids.ha.core.MProcessor;
import org.snu.ids.ha.core.Sentence;
import org.snu.ids.ha.core.Token;
import org.snu.ids.ha.core.Tokenizer;
import org.snu.ids.ha.core.UnlinkedCandidateException;
import org.snu.ids.ha.util.Timer;


public class KkmaTest
	extends TestCase
{
	String	str	= null;


	public void setUp()
	{
		str = "이들 법인들은 대부분 민법 제32조, 공익법인설립·운영에 관한 법률 제4조, 사회복지사업법 제16조에 따라 설립되었다.";
		str = "정거장에서 사건이 생기면, 그것은 이 소설의 배경이지만, 정거장이기 때문에 사건이 생기면, 그 정거장은 이 소설의 모티브가 되는 것이다.";
		str = "그 땐 단지 ‘IMF라는 것이 나쁜거구나’ 라고만 생각했지 그것이 정확히 무엇인지 몰랐습니다.";
		str = "집에 가는 사람";
		str = "팔이 가는 사람";
		str = "나는 밥을 먹었다";
		str = "하늘을 나는 새를 보았다.";
		str = "불의 잔";
		str = "김씨가";
		str = "내가 만일 안철수라면";
		str = "황수정 복귀";
	}


	public void testTokenizer()
	{
		System.out.println("testTokenizer====");
		CharArray charArray = new CharArray(str);

		Timer timer = new Timer();
		timer.start();
		List<Token> tl = Tokenizer.tokenize(charArray);
		for( Token tk : tl ) {
			System.out.println(tk);
		}
		timer.stop();
		timer.printMsg("Anal time");
		System.out.println();
		System.out.println();
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
			List<MCandidate> mlist = ma.analyze(str);
			for( MCandidate mc : mlist ) {
				if( mc.isWithSpace() ) System.out.println();
				System.out.println(mc.toDicString());
			}
			
			
			List<Sentence> analRet = mp.divide(mlist);
			for( Sentence sent : analRet ) {
				System.out.println(sent.getSentence());
				System.out.println(sent);
				List<Keyword> keywords = sent.getKeywords(2);
				for( Keyword keyword : keywords ) {
					System.out.println("\t" + keyword);
				}
			}
		} catch (UnlinkedCandidateException e) {
			System.err.println("TOKEN : " + e.token);
			System.err.println("PREVS : " + e.prevCands);
			System.err.println("MC    : \n\t" + e.mc);
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		timer.stop();
		timer.printMsg("Anal time");
		System.out.println();
		System.out.println();
	}
}
