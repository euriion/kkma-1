package org.snu.ids.ha.core;


import org.snu.ids.ha.constants.POSTag;


public class MAnalyzerMini
	extends MAnalyzerLite
{

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

		//System.out.println(token);

		// 한글에 대한 분석
		int len = token.length;
		CandidateList[] mem = new CandidateList[len];

		// 종결되지 않았는지 확인하여 종결되지 않은 경우에는 미등록 명사를 추가해 주기 위한 변수
		boolean onlyPartial = true;

		// 후보가 생성되지 않았을 때에는 조사를 저장하고 이를 활용하여 미등록어+조사 조합을 만들어줌.
		CandidateList unCands = new CandidateList();

		// implement longest word detection algorithm
		for( int start = 0, end = len - 1; end >= start && start >= 0; end-- ) {
			// init memory of i-th position
			mem[end] = new CandidateList();

			CharArray head = token.subCharArray(start, end - start + 1);
			MCandidate[] dicCands = getCands(head);

			// 후보에 대한 처리
			if( dicCands != null ) {
				for( MCandidate mc : dicCands ) {

					//System.out.println("\t" + mc);

					// find and set the best match
					if( (mc = getBestMC(start == 0 ? prevCands : mem[start - 1], mc)) != null ) {
						// add candidate
						mem[end].add(mc);

						// 종결되지 않았는지 확인
						if( end == len - 1 && onlyPartial && !mc.isLastTagOf(POSTag.V | POSTag.EP | POSTag.XP) ) {
							onlyPartial = false;
						}
					}
				}
			}

			//System.out.println(">>>" + head);
			//System.out.println(">>>" + mem[end]);

			// set next position
			if( mem[end].size() > 0 ) {
				start = end + 1;
				end = len;
			}
			// 문장이 발견되는 것이 없는 경우 미등록어 + 조사 형태로 만들어 줌
			else if( start == 0 && end + 1 < len ) {
				CharArray tail = token.subCharArray(end + 1, len - end - 1);
				dicCands = getCands(tail);
				if( dicCands != null ) {
					for( MCandidate mc : dicCands ) {
						if( mc.isLastTagOf(POSTag.J) ) {
							MCandidate un = getBestMC(prevCands, getCandUN(head));
							if( un != null && setUNPrevBestMC(un, mc) ) {
								unCands.add(mc);
							}
						}
					}
				}
			}
			// 이전 분석 후보가 있고, 마지막 위치인데도 분석이 안된 경우 이전 분석 후보의 글자수를 줄여서 찾아 줌.
			else if( start > 0 && end == start ) {
				do {
					// 직전 분석 위치 설정
					int prevIdx = end = start - 1;
					for( start = prevIdx; start > 0; start-- ) {
						// 이전 후보에 대한 분석 시작 위치 설정
						if( mem[start - 1] != null && mem[start - 1].size() > 0 ) {
							break;
						}
					}

					// 부적합한 결합에 대한 후보 삭제
					mem[prevIdx].clear();
				} while( start > 0 && start == end );
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
}
