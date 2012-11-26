package org.snu.ids.ha.core;


import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.dic.MorpDic;
import org.snu.ids.ha.dic.ProbDicSet;


public class MAnalyzerLite
	extends MAnalyzerFull
{
	public MAnalyzerLite()
	{
		dic = MorpDic.getInstance();
	}


	protected CandidateList analyze(CandidateList prevCands, Token token)
		throws Exception
	{
		CandidateList[] mem = null;

		// 한글 이외의 경우에 대한 분석
		if( token.getCharSet() != CharSetType.HANGUL ) {
			MCandidate mc = getCandOther(token);
			MCandidate org = mc;
			// set the best previous candidate
			mc = getBestMC(prevCands, mc);
			// if there is no previous bindable candidate, throw an exception
			if( mc == null ) throw new UnlinkedCandidateException(token, org, prevCands);

			// add a candidate
			return new CandidateList(mc);
		}

		// 한글에 대한 분석
		int len = token.length;
		mem = new CandidateList[len];

		// 종결되지 않았는지 확인하여 종결되지 않은 경우에는 미등록 명사를 추가해 주기 위한 변수
		boolean onlyPartial = true;

		// 후보가 생성되지 않았을 때에는 조사를 저장하고 이를 활용하여 미등록어+조사 조합을 만들어줌.
		CandidateList unCands = new CandidateList();

		// implement Viterbi algorithm
		for( int i = 0; i < len; i++ ) {
			// init memory of i-th position
			mem[i] = new CandidateList();

			for( int j = Math.max(0, i - MAX_WORD_LEN); j <= i; j++ ) {
				CharArray tail = token.subCharArray(j, i - j + 1);
				MCandidate[] dicCands = getCands(tail);
				if( dicCands == null ) continue;
				for( MCandidate mc : dicCands ) {

					//System.out.println("REVS :: " + (j == 0 ? prevCands : mem[j - 1]));
					//System.out.println("MC   :: " + mc);

					// find and set the best match
					MCandidate org = mc;
					if( (mc = getBestMC(j == 0 ? prevCands : mem[j - 1], mc)) != null ) {
						// add candidate
						mem[i].add(mc);

						// 종결되지 않았는지 확인
						if( i == len - 1 && onlyPartial && !mc.isLastTagOf(POSTag.V | POSTag.EP | POSTag.XP) ) {
							onlyPartial = false;
						}
					}
					// 분석 후보가 생성되지 않았지만 명사 + 조사가 될 수 있는 경우에 대한 후보 생성
					else if( j > 0 && i == len - 1 && org.isFirstTagOf(POSTag.J) ) {
						CharArray head = token.subCharArray(0, j);
						MCandidate un = getBestMC(prevCands, getCandUN(head));
						if( un != null && setUNPrevBestMC(un, org) ) {
							unCands.add(org);
						}
					}
				}
			}
		}

		// 접속 가능한 것이 없거나, 불완전한 어휘인 경우 미 등록 명사 추가
		if( mem[len - 1].size() == 0 || onlyPartial ) {
			MCandidate mc = getCandUN(token);
			MCandidate org = mc;
			mc = getBestMC(prevCands, mc);
			if( mc == null ) throw new UnlinkedCandidateException(token, org, prevCands);
			unCands.add(mc);
			return unCands;
		}

		return mem[len - 1];
	}


	/**
	 * <pre>
	 * 미등록어 명사를 이전 최적 후보로 설정해줌.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 2.
	 * @param prevMC
	 * @param mc
	 */
	protected boolean setUNPrevBestMC(MCandidate prevMC, MCandidate mc)
	{
		if( !prevMC.isApndbl(mc) ) return false;
		// set spacing lnpr
		float newLnprOfBestSpacing = prevMC.lnprOfBestSpacing + mc.lnprOfSpacing;
		newLnprOfBestSpacing += ProbDicSet.getLnprSyllableBi(prevMC.array[prevMC.start + prevMC.length - 1], mc.array[mc.start], false);

		// set tagging lnpr
		float newLnprOfBestTagging = prevMC.lnprOfBestTagging + mc.lnprOfTagging;
		newLnprOfBestTagging += ProbDicSet.getLnprPosGMorpIntra(prevMC.getLastTagNum(), mc.getFirstWord(), mc.getFirstTagNum());

		// set new best previous mc
		mc.prevBestMC = prevMC;
		mc.prevBestMC = prevMC;
		mc.withSpace = false;
		mc.lnprOfBestSpacing = newLnprOfBestSpacing;
		mc.lnprOfBestTagging = newLnprOfBestTagging;

		return true;
	}


	/**
	 * <pre>
	 * 띄어쓰기 안되어 있지만, 붙여서 빈번히 쓰이는 품사 조합인지 확인
	 * Lite 버젼에서는 일부에 대해서만 오류를 허용하고, 이외의 경우는 미등록 명사로 분석되도록 함.
	 * 	1) 명사 + 명사 (복합 명사)
	 *  2) 연결어미 + 보조 동사
	 *  3) 둘 중 하나가 기호인 경우
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 2.
	 * @param prevMC
	 * @param mc
	 * @return
	 */
	protected boolean isApndblWithSpacingError(MCandidate prevMC, MCandidate mc)
	{
		if(
		// 복합 명사인 경우
		prevMC.isLastTagOf(POSTag.NN) && mc.isFirstTagOf(POSTag.NN)
		// 연결 어미 + 보조 동사
		|| prevMC.isLastTagOf(POSTag.EC) && mc.isFirstTagOf(POSTag.VX)
		// 관형형 전성어미 + 의존 명사
		|| prevMC.isLastTagOf(POSTag.ETM) && mc.isFirstTagOf(POSTag.NNB)
		// 기호
		|| prevMC.isLastTagOf(POSTag.ETC | POSTag.ON) || mc.isFirstTagOf(POSTag.ETC | POSTag.ON) ) {
			return true;
		}
		return false;
	}


	/**
	 * <pre>
	 * 주어진 문자열에 해당하는 기분석 후보를 사전에서 찾아 반환한다.
	 * Lite 버젼에서는 사전에 어휘가 없을 때에는 단순히 null을 반환한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 2.
	 * @param charArray
	 * @return
	 */
	protected MCandidate[] getCands(CharArray charArray)
	{
		MCandidate[] ret = dic.get(charArray);
		if( ret != null ) {
			for( MCandidate mc : ret ) {
				mc.setCharArray(charArray);
			}
		}
		return ret;
	}
}
