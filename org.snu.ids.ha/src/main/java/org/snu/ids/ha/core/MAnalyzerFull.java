package org.snu.ids.ha.core;


import java.util.ArrayList;
import java.util.List;

import org.snu.ids.ha.constants.Condition;
import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.dic.MorpDic;
import org.snu.ids.ha.dic.ProbDicSet;
import org.snu.ids.ha.dic.SymbolDic;
import org.snu.ids.ha.util.Hangul;


public class MAnalyzerFull
{
	protected final static int	MAX_WORD_LEN	= 10;
	MorpDic						dic				= null;


	public MAnalyzerFull()
	{
		dic = MorpDic.getInstance();
	}


	public List<MCandidate> analyze(String str)
		throws Exception
	{
		// normalize string
		str = str.replaceAll("[ \t]", " ");

		// build char array
		CharArray charArray = new CharArray(str);

		// tokenize the given string
		List<Token> tkl = Tokenizer.tokenize(charArray);

		// analyze each tokens
		CandidateList prevLastAnal = null;
		for( Token tk : tkl ) {
			if( tk.getCharSet() == CharSetType.SPACE ) continue;
			prevLastAnal = analyze(prevLastAnal, tk);
		}

		// find the best result
		MCandidate bestMC = null;
		for( MCandidate mc : prevLastAnal ) {
			if( bestMC == null || bestMC.getLnprOfBest() < mc.getLnprOfBest() ) {
				bestMC = mc;
			}
		}

		// set the best result
		List<MCandidate> ret = new ArrayList<MCandidate>();
		while( bestMC != null ) {
			ret.add(0, bestMC);
			bestMC = bestMC.prevBestMC;
		}
		return ret;
	}
	
	
	public String getBestLinkStr(MCandidate mCand)
	{
		StringBuffer sb = new StringBuffer();
		for( MCandidate mc = mCand; mc != null; mc = mc.prevBestMC ) {
			sb.insert(0, (mc.withSpace ? "+ +" : "+") + mc.getMorpStr());
		}
		return sb.toString();
	}


	protected CandidateList analyze(CandidateList prevCands, Token token)
		throws Exception
	{
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
		CandidateList[] mem = new CandidateList[len];

		// implement Viterbi algorithm
		for( int i = 0; i < len; i++ ) {
			// init memory of i-th position
			mem[i] = new CandidateList();
			
			for( int j = Math.max(0, i - MAX_WORD_LEN); j <= i; j++ ) {
				CharArray tail = token.subCharArray(j, i - j + 1);
				for( MCandidate mc : getCands(tail) ) {
					// find and set the best match, then add to the analyzed list
					if( (mc = getBestMC(j == 0 ? prevCands : mem[j - 1], mc)) != null ) {
						mem[i].add(mc);
					}
				}
			}
		}

		// 종결되지 않았는지 확인하여 종결되지 않은 경우에는 미등록 명사를 추가해 줌
		boolean onlyPartial = true;
		// 최종 분석 결과가 1개 이상인 경우에만 종결 여부 확인, 최종 분석 결과가 없다면 미종결로 처리
		if( mem[len - 1].size() > 0 ) {
			for( MCandidate mc : mem[len - 1] ) {
				if( onlyPartial && !mc.isLastTagOf(POSTag.V | POSTag.EP | POSTag.XP) ) {
					onlyPartial = false;
					break;
				}
			}
		}

		// 불완전한 어휘인 경우 미 등록 명사 추가
		if( onlyPartial ) {
			MCandidate mc = getCandUN(token);
			MCandidate org = mc;
			mc = getBestMC(prevCands, mc);
			if( mc == null ) throw new UnlinkedCandidateException(token, org, prevCands);
			mem[len - 1].add(mc);
		}

		return mem[len - 1];
	}


	/**
	 * <pre>
	 * 최적 결합 조건을 만들어 내는 형태소 분석 결과를 찾는다.
	 * [시/EPH+ㄴ/ETM]+[다/EFN] 의 경우에는 
	 * [시/EPH+ㄴ다/EFN] 의 형태로 만들어 주어야 하기 때문에 이를 위한 처리를 추가한다.
	 * 이와 같이 변형 결합 되는 경우에 대해서는 태깅 확률을 달리 계산해준다.
	 * 유사 예) 모두 ㄴ, ㅁ, ㅂ, ㄹ 의 결합에 의한 변형임.
	 *     ㄴ다
	 *     ㄴ지
	 *     ㅁ다
	 *     ㅂ니다
	 *     ㄹ지
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 20.
	 * @param prevCands
	 * @param mc
	 * @param allowAppend
	 * @return
	 */
	protected MCandidate getBestMC(CandidateList prevCands, MCandidate mc)
		throws Exception
	{
		// 이전 분석 후보가 없는 경우, 시작 태그로 설정
		if( prevCands == null ) {
			// 조사, 어미, 접미사 등의 경우 앞과 결합해야함.
			if( mc.isFirstTagOf(POSTag.J | POSTag.E | POSTag.XS) ) return null;
			mc.prevBestMC = null;
			mc.lnprOfBestSpacing = mc.lnprOfSpacing;
			// TODO
			//mc.lnprOfBestTagging = ProbDicSet.getLnprPosGMorpInter(POSTag.BOS, mc.getWordAt(0), mc.getTagNumAt(0)) + mc.lnprOfTagging;
			mc.lnprOfBestTagging = mc.lnprOfTagging;
			return mc;
		}

		// 최적 결합을 만들어 낸 후보 생성
		MCandidate bestMC = null;

		// 초기 확률 값 설정
		mc.prevBestMC = null;
		mc.lnprOfBestSpacing = Integer.MIN_VALUE;
		mc.lnprOfBestTagging = Integer.MIN_VALUE;

		for( MCandidate prevMC : prevCands ) {

			//System.out.println("PREV " + prevMC);
			//System.out.println("MC   " + mc);

			boolean attached = false;

			// case 1) without space 붙여 쓰기에 대한 것은 한글은 실제로 붙여 쓰였을 때, 기호+한글조사는 공백과 함께 와도 붙여쓰기 허용
			if( prevMC.isApndbl(mc) // 붙여쓰기 가능한 품사인지 확인
					// 붙여 쓰인 경우 띄어쓰기 없이 결합 가능 
					&& (prevMC.start + prevMC.length == mc.start
					// (기호 또는 외국어 또는 이모티콘) + 조사의 경우 공백 무시하고 붙여 쓰기 허용
					|| prevMC.isLastTagOf(POSTag.ETC) && mc.isFirstTagOf(POSTag.J_VCP))
					// 이전에 따옴표로 이미 결합된 경우 조사만 결합 허용
					&& (!prevMC.isLastTagOf(POSTag.SS) || prevMC.isWithSpace() || mc.isFirstTagOf(POSTag.J_VCP)) ) {

				// case 1-1) 변형 통합되는 경우 like
				if(
				// 어미 + 어미 간의 결합으로 하나의 형태소로 복귀 : 시ㄴ+다 => 시+ㄴ다,
				prevMC.isLastTagOf(POSTag.EM) && mc.isFirstTagOf(POSTag.EM)
				// 는 + 다 => 는다 로 처리됨.
				|| prevMC.isHavingCond(Condition.NEUN) && mc.isFirstTagOf(POSTag.EM)
				// 부사화 접미사가 합쳐지면 부사로 만들어줌
				|| mc.isFirstTagOf(POSTag.XSM) ) {
					
					MCandidate tempMC = merge(prevMC, mc);

					// set spacing lnpr
					tempMC.lnprOfSpacing = prevMC.lnprOfSpacing + mc.lnprOfSpacing;
					float tempSpacingLnpr = ProbDicSet.getLnprSyllableBi(prevMC.array[prevMC.start + prevMC.length - 1], mc.array[mc.start], false);
					tempMC.lnprOfSpacing += tempSpacingLnpr;
					tempMC.lnprOfBestSpacing = prevMC.lnprOfBestSpacing + mc.lnprOfSpacing + tempSpacingLnpr;

					// set tagging lnpr
					tempMC.lnprOfTagging = ProbDicSet.getLnprOfTagging(tempMC.wordArr, tempMC.infoEncArr);
					if( tempMC.prevBestMC == null ) {
						// TODO
						//tempMC.lnprOfBestTagging = ProbDicSet.getLnprPosGMorpInter(POSTag.BOS, tempMC.getFirstWord(), tempMC.getFirstTagNum());
						tempMC.lnprOfBestTagging = tempMC.lnprOfTagging;
					} else {
						tempMC.lnprOfBestTagging = tempMC.prevBestMC.lnprOfBestTagging + tempMC.lnprOfTagging;
						if( tempMC.withSpace ) {
							tempMC.lnprOfBestTagging += ProbDicSet.getLnprPosGMorpInter(tempMC.prevBestMC.getLastTagNum(), tempMC.getFirstWord(), tempMC.getFirstTagNum());
						} else {
							tempMC.lnprOfBestTagging += ProbDicSet.getLnprPosGMorpIntra(tempMC.prevBestMC.getLastTagNum(), tempMC.getFirstWord(), tempMC.getFirstTagNum());
						}
					}

					// set new best previous mc
					if( bestMC == null || bestMC.getLnprOfBest() < tempMC.getLnprOfBest() ) {
						bestMC = tempMC;
						attached = true;
					}
				}
				// case 1-2) 일반 적인 결합
				else {
					// set spacing lnpr
					float newLnprOfBestSpacing = prevMC.lnprOfBestSpacing + mc.lnprOfSpacing;
					newLnprOfBestSpacing += ProbDicSet.getLnprSyllableBi(prevMC.array[prevMC.start + prevMC.length - 1], mc.array[mc.start], false);

					// set tagging lnpr
					float newLnprOfBestTagging = 0;
					float tempLnpr = ProbDicSet.getLnprMorpsGExp(prevMC.getLastWord(), prevMC.getLastTagNum(), mc.getFirstWord(), mc.getFirstTagNum());
					if( tempLnpr <= 0 ) {
						// 2012-12-02 probability computation looks like a bug
						//newLnprOfBestTagging = prevMC.lnprOfBestTagging - ProbDicSet.getLnprPosGExp(prevMC.getLastWord(), prevMC.getLastTagNum());
						newLnprOfBestTagging = prevMC.lnprOfBestTagging;
						newLnprOfBestTagging += mc.lnprOfTagging + tempLnpr - ProbDicSet.getLnprPosGExp(mc.getFirstWord(), mc.getFirstTagNum());
					} else {
						newLnprOfBestTagging = prevMC.lnprOfBestTagging + mc.lnprOfTagging;
						newLnprOfBestTagging += ProbDicSet.getLnprPosGMorpIntra(prevMC.getLastTagNum(), mc.getFirstWord(), mc.getFirstTagNum());
					}

					// set new best previous mc
					if( bestMC == null || bestMC.getLnprOfBest() < (newLnprOfBestSpacing + newLnprOfBestTagging) ) {
						mc.prevBestMC = prevMC;
						mc.withSpace = false;
						mc.lnprOfBestSpacing = newLnprOfBestSpacing;
						mc.lnprOfBestTagging = newLnprOfBestTagging;
						bestMC = mc;
						attached = true;
					}
				}

				//System.out.println("NOSPACING1 : " + prevMC);
				//System.out.println("NOSPACING2 : " + mc);
				//System.out.println("NOSPACING3 : " + bestMC);
			}

			// case 2) with space 띄어쓰기로 후보 생성 가능한지 확인.
			if( prevMC.isApndblWithSpace(mc)
			// 이전이 기호, 외래어, 이모티콘 등 하나의 결과를 만들어 내는 경우 붙여쓰여지지 않은 경우에만 결합 생성
			&& (!prevMC.isLastTagOf(POSTag.ETC | POSTag.ON) || !attached)
			// 현재 후보가 기호, 외래어, 이모티콘 등 하나의 결과를 만들어 내는 경우 붙여쓰여지지 않은 경우에만 결합 생성
			&& (!mc.isFirstTagOf(POSTag.ETC | POSTag.ON) || !attached)
			// 실제 띄어 쓰기 된 경우
			&& (prevMC.start + prevMC.length != mc.start || isApndblWithSpacingError(prevMC, mc)) ) {

				// set spacing lnpr
				float newLnprOfBestSpacing = prevMC.lnprOfBestSpacing + mc.lnprOfSpacing;
				newLnprOfBestSpacing += ProbDicSet.getLnprSyllableBi(prevMC.array[prevMC.start + prevMC.length - 1], mc.array[mc.start], true);

				// set tagging lnpr
				float newLnprOfBestTagging = prevMC.lnprOfBestTagging + mc.lnprOfTagging;
				newLnprOfBestTagging += ProbDicSet.getLnprPosGMorpInter(prevMC.getLastTagNum(), mc.getFirstWord(), mc.getFirstTagNum());

				// set new best previous mc
				if( bestMC == null || bestMC.getLnprOfBest() < (newLnprOfBestSpacing + newLnprOfBestTagging) ) {
					mc.prevBestMC = prevMC;
					mc.withSpace = true;
					mc.lnprOfBestSpacing = newLnprOfBestSpacing;
					mc.lnprOfBestTagging = newLnprOfBestTagging;
					bestMC = mc;
				}

				//System.out.println("SPACING1 : " + prevMC);
				//System.out.println("SPACING2 : " + mc);
				//System.out.println("SPACING2 : " + (newLnprOfBestSpacing + newLnprOfBestTagging));
				//System.out.println("SPACING3 : " + bestMC);
			}
			
			//System.out.println(mc + " " + getBestLinkStr(mc));
		}

		return bestMC;
	}


	/**
	 * <pre>
	 * 이전 분석 후보와 현재 분석 후보가 결합된 형태소를 만들 때 이를 합하여 하나의 분석 후보로 만들어 준다.
	 * 신다 : 시ㄴ+다 => 시+ㄴ다
	 * 는다 : 는+다 => 는다
	 * 가득히 : 가득+히 => 가득히
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 1.
	 * @param prevMC
	 * @param mc
	 * @return
	 */
	protected MCandidate merge(MCandidate prevMC, MCandidate mc)
	{
		MCandidate tempMC = new MCandidate();
		tempMC.array = mc.array;
		tempMC.start = prevMC.start;
		tempMC.length = mc.start + mc.length - prevMC.start;

		// merge morpheme array
		int prevMCWordLen = prevMC.wordArr.length, mcWordLen = mc.wordArr.length;

		// merge word array
		tempMC.wordArr = new char[prevMCWordLen + mcWordLen - 1][];
		for( int i = 0; i < prevMCWordLen - 1; i++ ) {
			tempMC.wordArr[i] = prevMC.wordArr[i];
		}
		char[] newWord = new char[prevMC.wordArr[prevMC.lastMorpIdx].length + mc.wordArr[0].length];
		System.arraycopy(prevMC.wordArr[prevMC.lastMorpIdx], 0, newWord, 0, prevMC.wordArr[prevMC.lastMorpIdx].length);
		System.arraycopy(mc.wordArr[0], 0, newWord, prevMC.wordArr[prevMC.lastMorpIdx].length, mc.wordArr[0].length);
		tempMC.wordArr[prevMCWordLen - 1] = newWord;
		for( int i = 1; i < mcWordLen; i++ ) {
			tempMC.wordArr[prevMCWordLen + i - 1] = mc.wordArr[i];
		}

		// merge tag array
		tempMC.infoEncArr = new long[prevMCWordLen + mcWordLen - 1];
		System.arraycopy(prevMC.infoEncArr, 0, tempMC.infoEncArr, 0, prevMCWordLen - 1);
		System.arraycopy(mc.infoEncArr, 0, tempMC.infoEncArr, prevMCWordLen - 1, mcWordLen);
		tempMC.lastMorpIdx = tempMC.wordArr.length - 1;

		// 결합 조건 설정
		tempMC.atlEnc = prevMC.atlEnc;
		tempMC.hclEnc = mc.hclEnc;
		tempMC.bclEnc = mc.bclEnc;
		tempMC.cclEnc = prevMC.cclEnc;
		tempMC.eclEnc = prevMC.eclEnc;
		// set spacing
		tempMC.withSpace = prevMC.withSpace;

		// 이전 최적 결합 설정
		tempMC.prevBestMC = prevMC.prevBestMC;

		return tempMC;
	}


	/**
	 * <pre>
	 * 띄어쓰기 안되어 있지만, 붙여서 빈번히 쓰이는 품사 조합인지 확인
	 * Full version에서는 에러가 있어도 모두 가능하도록 함.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 2.
	 * @param prevMC
	 * @param mc
	 * @return
	 */
	protected boolean isApndblWithSpacingError(MCandidate prevMC, MCandidate mc)
	{
		return true;
	}


	/**
	 * <pre>
	 * 해당 음절열에 대한 형태소 분석 후보를 찾아서 반환한다.
	 * 사전에 없는 경우에는 미등록어를 생성하여 반환해준다.
	 * 반드시 한글 음절열에 대해서 사용해야함.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 1.
	 * @param charArray
	 * @return
	 */
	protected MCandidate[] getCands(CharArray charArray)
	{
		MCandidate[] ret = dic.get(charArray);
		if( ret == null ) {
			// create and add new candidate
			ret = new MCandidate[] { getCandUN(charArray) };
		} else {
			for( MCandidate mc : ret ) {
				mc.setCharArray(charArray);
			}
		}
		return ret;
	}


	/**
	 * <pre>
	 * 한글 이외의 토큰에 대한 분석 후보를 생성하여 반환
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 31.
	 * @param token
	 * @return
	 */
	protected MCandidate getCandOther(Token token)
	{
		MCandidate mc = new MCandidate();
		mc.array = token.array;
		mc.start = token.start;
		mc.length = token.length;

		char[] word = token.getWord();
		long infoEnc = 0l;
		// 숫자 설정
		if( token.getCharSet() == CharSetType.NUMBER ) {
			infoEnc = POSTag.ON;
			// 숫자는 '제1조'와 같이 '제'라는 접두사가 부착될 수 있음.
			mc.addApndblTag(POSTag.XPN);
		}
		// 영문은 외국어로 설정
		else if( token.getCharSet() == CharSetType.ENGLISH || token.getCharSet() == CharSetType.COMBINED ) {
			infoEnc = POSTag.OL;
			mc.addApndblTag(POSTag.S);
		}
		// 한문은 한문으로 설정
		else if( token.getCharSet() == CharSetType.HANMUN ) {
			infoEnc = POSTag.OH;
			mc.addApndblTag(POSTag.S);
		}
		// 이모티콘은 이모티콘으로 설정해줌
		else if( token.getCharSet() == CharSetType.EMOTICON ) {
			infoEnc = POSTag.EMO;
			mc.addApndblTag(POSTag.S);
		}
		// 이외
		else {
			infoEnc = SymbolDic.getSymbolTag(word);
			// 기호인 경우 붙여 쓸 수 있는 것 설정해줌.
			mc.addApndblTag(POSTag.EC | POSTag.EF | POSTag.ET | POSTag.N | POSTag.M | POSTag.S | POSTag.O | POSTag.EMO);
		}

		// add morpheme
		mc.addMorp(word, infoEnc);
		// add basic having condition
		mc.addHavingCond(Condition.SET_FOR_UN);

		return mc;
	}


	/**
	 * <pre>
	 * 미등록 명사에 대한 분석 후보를 생성하여 반환
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 31.
	 * @param charArray
	 * @return
	 */
	protected MCandidate getCandUN(CharArray charArray)
	{
		MCandidate mc = new MCandidate();
		mc.addMorp(charArray.getWord(), POSTag.UN);
		mc.addApndblTag(mc.getBasicApndblTags());

		// 음운 조건 초기화
		int cond = 0;
		Hangul lastHg = Hangul.split(charArray.getLastChar());

		// 자음 조건 설정
		if( lastHg.hasJong() ) {
			cond |= Condition.JAEUM;
		} else {
			cond |= Condition.MOEUM;
		}

		if( lastHg.isPositiveMo() ) {
			cond |= Condition.YANGSEONG;
		} else {
			cond |= Condition.EUMSEONG;
		}

		// 'ㄹ'받침 체언 설정
		if( lastHg.jong == 'ㄹ' ) cond |= Condition.LIEUL;

		mc.addHavingCond(cond);
		mc.setLnprOfSpacing(ProbDicSet.getLnprOfSpacing(charArray));
		mc.setLnprOfTagging(ProbDicSet.getLnprNoun(mc.getFirstWord()));
		mc.setCharArray(charArray);
		return mc;
	}
}