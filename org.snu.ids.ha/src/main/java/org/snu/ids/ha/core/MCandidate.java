package org.snu.ids.ha.core;


import org.snu.ids.ha.constants.Condition;
import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.util.Convert;
import org.snu.ids.ha.util.Hangul;


/**
 * <pre>
 * 표현형에 대한 하나의 형태소 분석 후보를 저장한다.
 * 분석 후보 형태소 목록에 덧붙여 접속 조건, 합성 조건 등의 부가 정보를 저장한다.
 * </pre>
 * @author 	Dongjoo
 * @since	2011. 7. 14.
 */
public class MCandidate
{
	char[][]	wordArr		= null; // 형태 문자열
	long[]		infoEncArr	= null; // 품사 및 복합 명사 여부
	char[][]	compNounArr	= null; // 복합 명사에 대한 분해 결과
	int			lastMorpIdx	= -1;

	// 조건 정보 encoding
	long		atlEnc		= 0;	// 접속 가능한 품사 정보					[A]ppendable[T]ag[L]ist[Enc]oded
	int			hclEnc		= 0;	// 현재 후보가 가진 접속 조건				[H]aving[C]ondition[L]ist[Enc]oded
	int			cclEnc		= 0;	// 접속할 때 확인해야 하는 조건				[C]hecking[C]ondition[L]ist[Enc]oded
	int			eclEnc		= 0;	// 이전에 나오지 말아야 하는 조건			[E]xclusion[C]ondition[L]ist[Enc]oded
	int			bclEnc		= 0;	// 현재 후보가 뒷 후보와 확인해야 하는 조건 		[B]ackwardChecking[C]ondition[L]ist[Enc]oded


	public MCandidate()
	{
		super();
	}


	public void addMorp(String src)
	{
		char[] word = null;
		long infoEnc = 0l;
		if( src.startsWith("/") ) {
			word = new char[] { '/' };
			infoEnc = POSTag.getTagNum("SY");
		} else {
			String[] arr = src.split("/");
			word = arr[0].toCharArray();
			infoEnc = POSTag.getTagNum(arr[1]);
			if( arr.length > 2 && arr[2].equals("C") ) {
				infoEnc |= POSTag.COMPOSED;
			}
		}
		addMorp(word, infoEnc);
	}


	public void addMorp(String word, long tagNum)
	{
		this.addMorp(word.toCharArray(), tagNum);
	}


	public void addMorp(char[] word, long infoEnc)
	{
		if( wordArr == null ) {
			// add new morp
			wordArr = new char[1][];
			infoEncArr = new long[1];

			wordArr[0] = word;
			infoEncArr[0] = infoEnc;
			lastMorpIdx = 0;
		} else {
			// extend memory and add morp at last position
			char[][] tempWordList = wordArr;
			long[] tempInfoEncList = infoEncArr;

			wordArr = new char[tempWordList.length + 1][];
			infoEncArr = new long[tempInfoEncList.length + 1];

			System.arraycopy(tempWordList, 0, wordArr, 0, tempWordList.length);
			System.arraycopy(tempInfoEncList, 0, infoEncArr, 0, tempInfoEncList.length);

			lastMorpIdx++;
			wordArr[lastMorpIdx] = word;
			infoEncArr[lastMorpIdx] = infoEnc;
		}
	}


	public char[] getWordAt(int idx)
	{
		return wordArr[idx];
	}


	public String getStringAt(int idx)
	{
		return new String(wordArr[idx]);
	}


	public long getTagNumAt(int idx)
	{
		return infoEncArr[idx] & POSTag.MASK_TAG;
	}


	public String getTagAt(int idx)
	{
		return POSTag.getTag(getTagNumAt(idx));
	}


	public boolean isComposedAt(int idx)
	{
		return infoEncArr[idx] < 0;
	}


	public boolean isTagAt(int idx, long tagNum)
	{
		return getTagNumAt(idx) == tagNum;
	}


	public boolean isTagOfAt(int idx, long tagNum)
	{
		return (infoEncArr[idx] & POSTag.MASK_TAG & tagNum) > 0;
	}


	public boolean isWordOfAt(int idx, char[] word)
	{
		if( wordArr[idx].length != word.length ) return false;
		for( int i = 0; i < wordArr[idx].length; i++ ) {
			if( wordArr[idx][i] != word[i] ) return false;
		}
		return false;
	}


	public char[] getFirstWord()
	{
		return wordArr[0];
	}


	public char[] getLastWord()
	{
		return wordArr[lastMorpIdx];
	}


	public long getFirstTagNum()
	{
		return infoEncArr[0] & POSTag.MASK_TAG;
	}


	public long getLastTagNum()
	{
		return infoEncArr[lastMorpIdx] & POSTag.MASK_TAG;
	}


	public boolean isFirstTagOf(long tagNum)
	{
		return ((infoEncArr[0] & POSTag.MASK_TAG) & tagNum) > 0;
	}


	public boolean isLastTagOf(long tagNum)
	{
		return ((infoEncArr[lastMorpIdx] & POSTag.MASK_TAG) & tagNum) > 0;
	}


	public String getMorpStrAt(int idx)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(wordArr[idx]);
		sb.append("/" + getTagAt(idx));
		if( isComposedAt(idx) ) sb.append("/C");
		return sb.toString();
	}


	public String getMorpStr()
	{
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < wordArr.length; i++ ) {
			if( i > 0 ) sb.append('+');
			sb.append(wordArr[i]);
			sb.append("/" + getTagAt(i));
			if( isComposedAt(i) ) sb.append("/C");
		}
		return sb.toString();
	}


	public char[][] getWordArr()
	{
		return wordArr;
	}


	public void setWordArr(char[][] wordArr)
	{
		this.wordArr = wordArr;
	}


	public long[] getInfoEncArr()
	{
		return infoEncArr;
	}


	public void setInfoEncArr(long[] infoEncArr)
	{
		this.infoEncArr = infoEncArr;
	}


	public char[][] getCompNounArr()
	{
		return compNounArr;
	}


	public void setCompNounArr(char[][] compNounArr)
	{
		this.compNounArr = compNounArr;
	}


	public int getLastMorpIdx()
	{
		return lastMorpIdx;
	}


	public void setLastMorpIdx(int lastMorpIdx)
	{
		this.lastMorpIdx = lastMorpIdx;
	}


	public int size()
	{
		return wordArr == null ? 0 : wordArr.length;
	}


	public String getATL()
	{
		return POSTag.getTagStr(atlEnc);
	}


	public long getATLEnc()
	{
		return atlEnc;
	}


	public String getHCL()
	{
		return Condition.getCondStr(hclEnc);
	}


	public int getHCLEnc()
	{
		return hclEnc;
	}


	public String getCCL()
	{
		return Condition.getCondStr(cclEnc);
	}


	public int getCCLEnc()
	{
		return cclEnc;
	}


	public String getECL()
	{
		return Condition.getCondStr(eclEnc);
	}


	public int getECLEnc()
	{
		return eclEnc;
	}


	/**
	 * <pre>
	 * 접속 가능한 품사정보를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param prevTag
	 */
	public void addApndblTag(String tag)
	{
		addApndblTag(POSTag.getTagNum(tag));
	}


	/**
	 * <pre>
	 * 접속 가능한 품사정보를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param tag
	 */
	public void addApndblTag(long tagNum)
	{
		atlEnc |= tagNum;
	}


	/**
	 * <pre>
	 * 접속 가능한 품사정보를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param tags
	 */
	public void addApndblTags(String[] tags)
	{
		for( int i = 0, stop = tags.length; i < stop; i++ ) {
			addApndblTag(tags[i]);
		}
	}


	/**
	 * <pre>
	 * 기분석 결과가 가지는 접속 조건을 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param cond
	 */
	public void addHavingCond(String cond)
	{
		addHavingCond(Condition.getCondNum(cond));
	}


	/**
	 * <pre>
	 * 기분석 결과가 가지는 접속 조건을 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param conds
	 */
	public void addHavingConds(String[] conds)
	{
		for( int i = 0, stop = conds.length; i < stop; i++ ) {
			addHavingCond(conds[i]);
		}
	}


	/**
	 * <pre>
	 * 기분석 결과가 가지는 접속 조건을 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param condNum
	 */
	public void addHavingCond(int condNum)
	{
		hclEnc |= condNum;
	}


	/**
	 * <pre>
	 * 주어진 조건을 가지고 있는지 확인
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param condNum
	 * @return
	 */
	public boolean isHavingCond(int condNum)
	{
		return Condition.checkAnd(hclEnc, condNum);
	}


	/**
	 * <pre>
	 * 후보 기분석 결과가 가진 조건 정보를 삭제한다.
	 * @since 2009. 10. 15
	 * 동시에 뒷결합 조건도 삭제한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 */
	public void clearHavingCondition()
	{
		this.hclEnc = 0;
		this.bclEnc = 0;
	}


	public void addChkCond(int cond)
	{
		cclEnc |= cond;
	}


	/**
	 * <pre>
	 * 기분석 결과가 접속시 확인해야하는 조건 정보를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param cond
	 */
	public void addChkCond(String cond)
	{
		cclEnc |= Condition.getCondNum(cond);
	}


	/**
	 * <pre>
	 * 기분석 결과가 접속시 확인해야하는 조건 정보를 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @param conds
	 */
	public void addChkConds(String[] conds)
	{
		for( int i = 0, stop = conds.length; i < stop; i++ ) {
			addChkCond(conds[i]);
		}
	}


	/**
	 * <pre>
	 * 후의 결합 조건 추가
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 15.
	 * @param cond
	 */
	public void addBackwardCond(int cond)
	{
		bclEnc |= cond;
	}


	public void addExclusionCond(int cond)
	{
		eclEnc |= cond;
	}


	/**
	 * <pre>
	 * 접속 배제 조건을 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 15
	 * @param cond
	 */
	public void addExclusionCond(String cond)
	{
		eclEnc |= Condition.getCondNum(cond);
	}


	/**
	 * <pre>
	 * 배열로 주어진 배제 조건을 추가한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 15
	 * @param conds
	 */
	public void addExclusionConds(String[] conds)
	{
		for( int i = 0, stop = conds.length; i < stop; i++ ) {
			addExclusionCond(conds[i]);
		}
	}


	/**
	 * <pre>
	 * 배제 조건에 해당하여 접속이 불가능한지 확인한다.
	 * 하나라도 조건을 만족하면 배제해야하는 것으로 간주.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 14
	 * @param exlCondEnc
	 * @return
	 */
	public boolean isCondExclusive(int exlCondEnc)
	{
		if( exlCondEnc == 0 ) return false;
		return Condition.checkOr(hclEnc, exlCondEnc);
	}


	/**
	 * <pre>
	 * 자모 조건을 생성해준다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 */
	public void initConds(String string)
	{
		// atlEnc: 품사별 기본 접속 가능 품사 초기화	
		initBasicApndblTags();
		initBasicPhonemeCond(string);
		initBasicBackwardCond();
	}


	/**
	 * <pre>
	 * 결합 가능한 기본 품사를 초기화 한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 15.
	 */
	public void initBasicApndblTags()
	{
		addApndblTag(getBasicApndblTags());
	}


	/**
	 * <pre>
	 * 결합 음운 조건을 초기화 한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2009. 10. 19
	 * @param prevWord
	 */
	public void initBasicPhonemeCond(String string)
	{
		// hclEnc: 음운 정보 초기화
		addHavingCond(getBasicPhonemeConds(string));
	}


	public void initBasicBackwardCond()
	{
		if( isLastTagOf(POSTag.ETM | POSTag.ETN) )
			bclEnc |= (this.hclEnc & (Condition.NIEUN | Condition.LIEUL | Condition.MIEUM));
		else if( isLastTagOf(POSTag.V) ) {
			// 'ㄹ', 'ㅎ' 탈락에 대한 후위 조건 확인
			bclEnc |= (this.hclEnc & Condition.MINUS_JA_SET);
			// 'ㅂ' 추가에 의한 후위 조건 확인
			if( Condition.checkAnd(hclEnc, Condition.BIEUB) ) {
				Hangul mpLastCh = Hangul.split(wordArr[lastMorpIdx][wordArr[lastMorpIdx].length - 1]);
				if( mpLastCh.jong != 'ㅂ' ) bclEnc |= Condition.BIEUB;
			}
		}
	}


	/**
	 * <pre>
	 * 각 품사별로 띄어쓰기 없이 이전에 올 수 있는 품사를 설정해줌.  
	 * </pre>
	 * @author	therocks
	 * @since	2009. 09. 30
	 * @return the basic appendable prevTag with encoding
	 */
	public long getBasicApndblTags()
	{
		// 모든 한글은 따옴표, 괄호표, 줄표, 붙임표(물결, 숨김, 빠짐) 표와 붙여 쓸 수 있음.
		long tags = POSTag.SS | POSTag.SO;

		// 체언은 접두어를 가질 수 있음.
		if( isFirstTagOf(POSTag.NNA | POSTag.XR) ) {
			tags |= POSTag.XPN;
			// 보통 명사는 복합어를 만들어 낼 수 있음.
			if( isFirstTagOf(POSTag.NNG) ) tags |= POSTag.NNG;
		}
		// 용언(어근 포함)은 용언 접두어를 가질 수 있음
		else if( isFirstTagOf(POSTag.VV | POSTag.VA | POSTag.XR) ) {
			tags |= POSTag.XPV;
		}
		// 명사형 접미사는 명사와 접속 가능
		else if( isFirstTagOf(POSTag.XSN) ) {
			tags |= POSTag.NNA | POSTag.UN | POSTag.NNM | POSTag.NNM;
		}
		// 용언 접미사는 어근과 결합 가능
		else if( isFirstTagOf(POSTag.XSA | POSTag.XSV) ) {
			tags |= POSTag.NN | POSTag.XR | POSTag.MAG;
		}
		// 단위 의존 명사는 수사와 붙여서 쓰는 것 용인
		else if( isFirstTagOf(POSTag.NNM | POSTag.NR) ) {
			tags |= POSTag.NR | POSTag.ON;
		}
		// 외국어에 조사 붙여 쓰일 수 있음.
		else if( isFirstTagOf(POSTag.J) ) {
			tags |= POSTag.O | POSTag.NR;
		}

		// 명사는 가운데 점과 연결 가능
		if( isFirstTagOf(POSTag.N) ) {
			tags |= POSTag.SP;
		}

		return tags;
	}


	/**
	 * <pre>
	 * 기본 음운 정보 조건을 encoding 하여 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 09. 30
	 * @param prevWord
	 * @return
	 */
	public int getBasicPhonemeConds(String string)
	{
		int cond = 0;
		char lastCh = string.charAt(string.length() - 1);
		Hangul lastHg = Hangul.split(lastCh);

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

		// 동사, 형용사에 대한 추가 설정
		if( isLastTagOf(POSTag.VP) ) {
			// 동사, 형용사의 겹모음은 었을 붙여주기 위해 자음 조건 추가해줌
			if( lastHg.isDoubleMo() ) cond |= Condition.JAEUM;

			char morpLastCh = wordArr[lastMorpIdx][wordArr[lastMorpIdx].length - 1];

			// '하다'동사에 대한 처리
			if( morpLastCh == '하' ) {
				cond |= Condition.HA;
			}
			// '가다'동사에 대한 처리
			else if( morpLastCh == '가' ) {
				cond |= Condition.GADA;
			}
			// '오다'동사에 대한 처리
			else if( morpLastCh == '오' ) {
				cond |= Condition.ODA;
			}
			// 'ㄹ'받침 용언 설정
			else if( lastHg.jong == 'ㄹ' ) {
				cond |= Condition.LIEUL;
			}
		}
		// 체언 조건 설정
		else if( isLastTagOf(POSTag.N) ) {
			// 'ㄹ'받침 체언 설정
			if( lastHg.jong == 'ㄹ' ) {
				cond |= Condition.LIEUL;
			}
		}
		// 어미에 대한 추가 설정
		else if( isLastTagOf(POSTag.ET) && wordArr[lastMorpIdx].length == 1 ) {
			if( wordArr[lastMorpIdx][0] == 'ㄴ' ) {
				cond |= Condition.NIEUN;
			} else if( wordArr[lastMorpIdx][0] == 'ㄹ' ) {
				cond |= Condition.LIEUL;
			} else if( wordArr[lastMorpIdx][0] == 'ㅁ' ) {
				cond |= Condition.MIEUM;
			}
		}

		return cond;
	}


	/**
	 * <pre>
	 * 접속 가능한 조건인지 확인
	 * 1) lastMorp.isTagOf(mcToAppend.atlEnc)
	 *    뒷 분석 후보의 접속 가능 품사로 끝나는지 확인
	 * 2) isCondApndbl(mcToAppend.cclEnc)
	 *    뒷 분석 후보가 확인해야하는 조건이 있을 때, 이를 만족하는지 확인
	 * 3) !isCondExclusive(mcToAppend.eclEnc)
	 *    뒷 분석 후보가 배제 조건을 가질 때, 이를 하나라도 만족하는지 확인
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 27
	 * @param mcToAppend
	 * @return
	 */
	public boolean isApndbl(MCandidate mcToAppend)
	{
		if(
		// 어절 종결 조건인지 확인
		isHavingCond(Condition.F)
		// 결합 가능 품사인지 확인
		|| !isLastTagOf(mcToAppend.atlEnc)
		// 결합 가능 조건인지 확인
		|| !Condition.checkAnd(hclEnc, mcToAppend.cclEnc)
		// 배제 조건인지 확인
		|| isCondExclusive(mcToAppend.eclEnc)
		// 어간의 변형을 동반한 활용에 의한 어미와의 결합인지 확인 
		|| mcToAppend.isFirstTagOf(POSTag.E) && !Condition.checkAnd(mcToAppend.cclEnc, bclEnc) ) {
			return false;
		}
		return true;
	}


	/**
	 * <pre>
	 * 띄어쓰기가 되었을 때 연결이 가능한지 확인
	 * 마지막이 활용의 시작이면, 다음은 반드시 선어말 혹은 어말 어미가 와야 함!!
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param mcToAppend
	 * @return
	 */
	boolean isApndblWithSpace(MCandidate mcToAppend)
	{
		// 띄어쓰기 불가능 확인
		if(
		// 어간, 선어말 어미, 접두사의 경우 뒷부분에 띄어쓰기를 추가하지 못함.
		isLastTagOf(POSTag.V | POSTag.EP | POSTag.XP)
		// 어미, 조사, 접미사,  
		|| mcToAppend.isFirstTagOf(POSTag.E | POSTag.XS | POSTag.VCP | POSTag.J)
		// 생략되어서 앞말과 이어져야 하는 경우
		|| mcToAppend.isHavingCond(Condition.SHORTEN) ) {
			return false;
		}
		return true;
	}


	// 표층어 정보
	char[]		array				= null;		// 입력된 전체 문자열
	int			start				= 0;		// 표층어의 시작 시점
	int			length				= 0;		// 표층어의 종료 시점
	float		lnprOfSpacing		= 0;		// 현재 표층어의 띄어쓰기 확률
	float		lnprOfTagging		= 0;		// 현재 분석 후보의 품사 부착 확률
	float		lnprOfBestSpacing	= 0;		// 이전까지의 모든 표층어 분석 후보에 대한 띄어쓰기 최적 확률
	float		lnprOfBestTagging	= 0;		// 이전까지의 모든 표층어 분석 후보에 대한 품사 부착 확률 
	MCandidate	prevBestMC			= null;		// 이전 최적 형태소 분석 노드
	boolean		withSpace			= false;	// 띄어쓰기 여부


	public char[] getArray()
	{
		return array;
	}


	public int getStart()
	{
		return start;
	}


	public int getLength()
	{
		return length;
	}


	public String getExp()
	{
		return new String(array, start, length);
	}


	public float getLnprOfSpacing()
	{
		return lnprOfSpacing;
	}


	public void setLnprOfSpacing(float lnprOfSpacing)
	{
		this.lnprOfSpacing = lnprOfSpacing;
	}


	public float getLnprOfTagging()
	{
		return lnprOfTagging;
	}


	public void setLnprOfTagging(float lnprOfTagging)
	{
		this.lnprOfTagging = lnprOfTagging;
	}


	public float getLnpr()
	{
		return lnprOfSpacing + lnprOfTagging;
	}


	public MCandidate getPrevBestMC()
	{
		return prevBestMC;
	}


	public void setPrevBestMC(MCandidate prevBestMC)
	{
		this.prevBestMC = prevBestMC;
	}


	public float getLnprOfBestSpacing()
	{
		return lnprOfBestSpacing;
	}


	public void setLnprOfBestSpacing(float lnprOfBestSpacing)
	{
		this.lnprOfBestSpacing = lnprOfBestSpacing;
	}


	public float getLnprOfBestTagging()
	{
		return lnprOfBestTagging;
	}


	public void setLnprOfBestTagging(float lnprOfBestTagging)
	{
		this.lnprOfBestTagging = lnprOfBestTagging;
	}


	public float getLnprOfBest()
	{
		return lnprOfBestSpacing + lnprOfBestTagging;
	}


	public void setCharArray(CharArray charArray)
	{
		this.array = charArray.array;
		this.start = charArray.start;
		this.length = charArray.length;
	}


	public boolean isWithSpace()
	{
		return withSpace;
	}


	public MCandidate clone()
	{
		MCandidate clone = new MCandidate();
		clone.wordArr = new char[this.wordArr.length][];
		System.arraycopy(this.wordArr, 0, clone.wordArr, 0, this.wordArr.length);
		clone.infoEncArr = new long[this.infoEncArr.length];
		System.arraycopy(this.infoEncArr, 0, clone.infoEncArr, 0, this.infoEncArr.length);
		clone.compNounArr = this.compNounArr;
		clone.lastMorpIdx = this.lastMorpIdx;

		clone.atlEnc = this.atlEnc;
		clone.hclEnc = this.hclEnc;
		clone.cclEnc = this.cclEnc;
		clone.bclEnc = this.bclEnc;
		clone.eclEnc = this.eclEnc;
		clone.lnprOfSpacing = this.lnprOfSpacing;
		clone.lnprOfTagging = this.lnprOfTagging;
		clone.prevBestMC = this.prevBestMC;
		clone.lnprOfBestSpacing = this.lnprOfBestSpacing;
		clone.lnprOfBestTagging = this.lnprOfBestTagging;
		return clone;
	}


	/**
	 * 띄어쓰기 없이 접속 가능한 품사 정보
	 */
	public static final String	DLMT_ATL	= "#";
	/**
	 * 현재 후보가 가지는 조건 정보
	 */
	public static final String	DLMT_HCL	= "&";
	/**
	 * 접속할 때 확인해야하는 조건 정보 뒤로 맞출 때 확인해야함.
	 */
	public static final String	DLMT_BCL	= "~";
	/**
	 * 접속할 때 확인해야하는 조건 정보
	 */
	public static final String	DLMT_CCL	= "@";
	/**
	 * 접합 배재 조건 정보
	 */
	public static final String	DLMT_ECL	= "￢";
	/**
	 * 음절열에 대한 형태소 분석 확률 값
	 */
	public static final String	DLMT_PRB	= "%";
	/**
	 * 복합명사 목록
	 */
	public static final String	DLMT_CNL	= "$";


	/**
	 * <pre>
	 * 기분석 후보 정보를 반환한다.
	 * 분석 사전에서 { } 내에 들어갈 정보를 반환해준다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 4
	 * @return
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();

		sb.append(String.format("%4d", wordArr.length));
		sb.append(String.format("%8.2f", lnprOfSpacing));
		sb.append(String.format("%8.2f", lnprOfTagging));
		sb.append(String.format("%8.2f", getLnpr()));
		sb.append(String.format("%8.2f", lnprOfBestSpacing));
		sb.append(String.format("%8.2f", lnprOfBestTagging));
		sb.append(String.format("%8.2f  ", getLnprOfBest()));

		// 형태소 분석 결과
		sb.append("[" + getMorpStr() + "]");

		// 접속 가능한 품사 정보
		String temp = POSTag.getZipTagStr(atlEnc);
		if( temp != null ) sb.append(DLMT_ATL + "(" + temp + ")");

		// 현재 후보가 가진 접속 조건
		temp = Condition.getCondStr(hclEnc);
		if( temp != null ) sb.append(DLMT_HCL + "(" + temp + ")");

		// 뒷방향 접속 조건 확인
		temp = Condition.getCondStr(bclEnc);
		if( temp != null ) sb.append(DLMT_BCL + "(" + temp + ")");

		// 접속할 때 확인해야 하는 조건
		temp = Condition.getCondStr(cclEnc);
		if( temp != null ) sb.append(DLMT_CCL + "(" + temp + ")");

		// 접속할 때 배제해야 하는 조건
		temp = Condition.getCondStr(eclEnc);
		if( temp != null ) sb.append(DLMT_ECL + "(" + temp + ")");

		// 복합명사
		if( compNounArr != null ) {
			sb.append(DLMT_CNL + "(");
			for( int i = 0; i < compNounArr.length; i++ ) {
				if( i > 0 ) sb.append('+');
				sb.append(compNounArr[i]);
			}
			sb.append(")");
		}

		if( prevBestMC != null ) {
			// TODO
			//sb.append("\t" + prevBestMC.lastMorp);
		}

		return sb.toString();
	}


	public String toDicString()
	{
		StringBuffer sb = new StringBuffer();

		// 형태소 분석 결과
		sb.append("[" + getMorpStr() + "]");

		// 접속 가능한 품사 정보
		String temp = POSTag.getZipTagStr(atlEnc);
		if( temp != null ) sb.append(DLMT_ATL + "(" + temp + ")");

		// 현재 후보가 가진 접속 조건
		temp = Condition.getCondStr(hclEnc);
		if( temp != null ) sb.append(DLMT_HCL + "(" + temp + ")");

		// 뒷방향 접속 조건 확인
		temp = Condition.getCondStr(bclEnc);
		if( temp != null ) sb.append(DLMT_BCL + "(" + temp + ")");

		// 접속할 때 확인해야 하는 조건
		temp = Condition.getCondStr(cclEnc);
		if( temp != null ) sb.append(DLMT_CCL + "(" + temp + ")");

		// 접속할 때 배제해야 하는 조건
		temp = Condition.getCondStr(eclEnc);
		if( temp != null ) sb.append(DLMT_ECL + "(" + temp + ")");

		// 복합명사
		if( compNounArr != null ) {
			sb.append(DLMT_CNL + "(");
			for( int i = 0; i < compNounArr.length; i++ ) {
				if( i > 0 ) sb.append('+');
				sb.append(compNounArr[i]);
			}
			sb.append(")");
		}

		// 확률 정보
		sb.append(DLMT_PRB + String.format("(%.2f,%.2f)", lnprOfSpacing, lnprOfTagging));

		return sb.toString();
	}


	/**
	 * <pre>
	 * 사전 bin...
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 23.
	 * @return
	 */
	public byte[] toBytes()
	{

		// 형태소 분석 결과 갯수 + 복합 명사 갯수 저장으로 시작
		int byteSize = 2;
		// 형태소 분석 결과 크기
		for( char[] word : wordArr ) {
			byteSize += word.length * Convert.CHA_SIZE + 1 + Convert.LNG_SIZE;
		}
		// 복합 명사 분석 결과 크기
		if( compNounArr != null ) {
			for( int i = 0; i < compNounArr.length; i++ ) {
				byteSize += compNounArr[i].length * Convert.CHA_SIZE + 1;
			}
		}
		// 각종 조건 저장 공간
		byteSize += Convert.LNG_SIZE + 4 * Convert.INT_SIZE;
		// 분석 확률 저장 공간
		byteSize += Convert.FLT_SIZE;

		// set memory
		byte[] ret = new byte[byteSize];
		int offset = 0;
		byte[] temp = null;

		ret[offset] = (byte) wordArr.length;
		offset++;

		// 형태소 분석 결과
		for( char[] word : wordArr ) {
			ret[offset] = (byte) word.length;
			offset++;
			temp = Convert.toByta(word);
			System.arraycopy(temp, 0, ret, offset, temp.length);
			offset += temp.length;
		}
		temp = Convert.toByta(infoEncArr);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;

		// 복합명사
		if( compNounArr != null ) {
			ret[offset] = (byte) compNounArr.length;
			offset++;
			for( int i = 0; i < compNounArr.length; i++ ) {
				ret[offset] = (byte) compNounArr[i].length;
				offset++;
				temp = Convert.toByta(compNounArr[i]);
				System.arraycopy(temp, 0, ret, offset, temp.length);
				offset += temp.length;
			}
		} else {
			ret[offset] = (byte) 0;
			offset++;
		}

		temp = Convert.toByta(atlEnc);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;
		temp = Convert.toByta(hclEnc);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;
		temp = Convert.toByta(bclEnc);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;
		temp = Convert.toByta(cclEnc);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;
		temp = Convert.toByta(eclEnc);
		System.arraycopy(temp, 0, ret, offset, temp.length);
		offset += temp.length;
		temp = Convert.toByta(lnprOfTagging);
		System.arraycopy(temp, 0, ret, offset, temp.length);

		return ret;
	}
}