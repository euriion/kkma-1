package org.snu.ids.ha.constants;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.snu.ids.ha.util.Hangul;



/**
 * <pre>
 * 형태소간 결합 조건을 인코딩하는 클래스
 * int 즉 32bit 값에 조건들을 OR로 지정하도록 한다.
 * </pre>
 * @author 	therocks
 * @since	2009. 10. 13
 */
public class Condition
	extends Hangul
{
	public static final String[] COND_ARR = { 
			"모음", 		// 01 마지막 음절이 종성을 가지지 않음
			"자음", 		// 02 마지막 음절이 종성을 가짐
			"양성", 		// 03 마지막 음절이 양성 모음
			"음성", 		// 04 마지막 음절이 음성 모음
			"아", 		// 05 어말어미 '아'로 종결됨
			"었", 		// 06 선어말 어미 '었'이 부착됨
			"겠", 		// 07 선어말 어미 '겠'이 부착됨
			"는", 		// 08 선어말 어미 '는'이 부착됨
			"려", 		// 09 려로 끝나서 다음에 '하+어미'형태로 준말
			"ㄴ",		// 10
			"ㄹ",		// 11
			"ㅁ",		// 12
			"ㅂ",		// 13
			"-ㄹ", 		// 14 자음 'ㄹ'이 탈락함
			"-ㅂ",		// 15 ㅂ 불규칙, 가깝 -> 가까우
			"-ㅅ", 		// 16 자음 'ㅅ'이 탈락함
			"-ㅎ", 		// 17 자음 'ㅎ'이 탈락함
			"하", 		// 18 '하'로 끝나는 용언
			"가다", 		// 19 '가다'로 끝나는 용언
			"오다", 		// 20 '오다'로 끝나는 용언
			"F", 		// 21 보조사 '는'과 같이 어절의 끝에 오는 형태소를 위해 사용
			"생략" 		// 22 바다(이)다와 같이 (이)가 가운데 생략된 것을 표현하기 위한 기분석 후보를 표현, 생략된 것을 위한 기분석 후보는 띄어쓰기 될 수 없음.
		};

	public static final Hashtable<String, Integer>	COND_HASH		= new Hashtable<String, Integer>();
	public static final Hashtable<Integer, String>	COND_NUM_HASH	= new Hashtable<Integer, String>();
	static {
		int conditionNum = 0;
		// 일반 조건 생성
		for( int i = 0, stop = COND_ARR.length; i < stop; i++ ) {
			conditionNum = 1 << i;
			COND_HASH.put(COND_ARR[i], new Integer(conditionNum));
			COND_NUM_HASH.put(new Integer(conditionNum), COND_ARR[i]);
		}
	}


	/**
	 * <pre>
	 * i 번째 조건을 인코딩하는 int값을 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 19
	 * @param i
	 * @return
	 */
	private static final int getCondNum(int i)
	{
		return (1 << i);
	}


	/**
	 * <pre>
	 * 주어진 조건 정보에 대한 대한 int number를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param cond
	 * @return
	 */
	public static int getCondNum(String cond)
	{
		try {
			return COND_HASH.get(cond);
		} catch (Exception e) {
			System.err.println("[" + cond + "] 정의되지 않은 조건입니다.");
		}
		return 0;
	}


	/**
	 * <pre>
	 * 조건을 가지는 문자열을 받아들여서 이에 해당하는 인코딩된 값을 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 19
	 * @param conds
	 * @return
	 */
	public static int getCondNum(String[] conds)
	{
		int ival = 0;
		for( int i = 0, size = (conds == null ? 0 : conds.length); i < size; i++ ) {
			ival |= getCondNum(conds[i]);
		}
		return ival;
	}


	/**
	 * <pre>
	 * condNum에 대한 조건 문자를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param condNum
	 * @return
	 */
	public static String getCond(int condNum)
	{
		return condNum == 0 ? null : COND_NUM_HASH.get(new Long(condNum));
	}


	/**
	 * <pre>
	 * 인코딩된 조건값이 나타내는 조건 목록을 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 6. 6
	 * @param encCondNum
	 * @return
	 */
	public static List<String> getCondList(int encCondNum)
	{
		List<String> ret = new ArrayList<String>();
		for( int i = 0, stop = COND_ARR.length; i < stop; i++ ) {
			if( (encCondNum & getCondNum(i)) > 0 ) ret.add(COND_ARR[i]);
		}
		return ret;
	}


	/**
	 * <pre>
	 * 인코딩된 조건값을 문자열로 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 19
	 * @param encCondNum
	 * @return
	 */
	public static String getCondStr(int encCondNum)
	{
		StringBuffer sb = new StringBuffer();
		List<String> condList = getCondList(encCondNum);
		for( int i = 0, size = condList.size(); i < size; i++ ) {
			if( i > 0 ) sb.append(",");
			sb.append(condList.get(i));
		}
		return sb.length() == 0 ? null : sb.toString();
	}


	/**
	 * <pre>
	 * 가능한 모든 조건을 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 13
	 * @return
	 */
	public static final List<String> getCondList()
	{
		List<String> condList = new ArrayList<String>();
		List<Integer> condNumList = new ArrayList<Integer>(COND_NUM_HASH.keySet());
		Collections.sort(condNumList);
		for( int i = 0, size = condNumList.size(); i < size; i++ ) {
			condList.add(COND_NUM_HASH.get(condNumList.get(i)));
		}
		return condList;
	}


	/**
	 * <pre>
	 * 조건을 만족하는지 여부를 확인하여 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 13
	 * @param havingCond
	 * @param checkingCond
	 * @return
	 */
	public static final boolean checkAnd(int havingCond, int checkingCond)
	{
		return (havingCond & checkingCond) == checkingCond;
	}


	/**
	 * <pre>
	 * 해당 조건을 만족하는 것이 하나라도 있는지의 여부를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2009. 10. 15
	 * @param havingCond
	 * @param checkingCond
	 * @return
	 */
	public static final boolean checkOr(int havingCond, int checkingCond)
	{
		return (havingCond & checkingCond) > 0;
	}


	/**
	 * 조건 값들을 저장해두고, 사용하도록 한다.
	 */
	// 음운 조건
	public static final int	MOEUM			= getCondNum("모음");
	public static final int	JAEUM			= getCondNum("자음");
	public static final int	YANGSEONG		= getCondNum("양성");
	public static final int	EUMSEONG		= getCondNum("음성");
	// 어미 조건
	public static final int	AH				= getCondNum("아");
	public static final int	EUT				= getCondNum("었");
	public static final int	GET				= getCondNum("겠");
	public static final int	NEUN			= getCondNum("는");
	public static final int	LYEO			= getCondNum("려");
	
	// 모음으로 끝나는 어간에 'ㄴ', 'ㄹ', 'ㅁ'을 합쳐서  
	public static final int	NIEUN			= getCondNum("ㄴ");
	public static final int	LIEUL			= getCondNum("ㄹ");
	public static final int	MIEUM			= getCondNum("ㅁ");
	// 어간에 ㅂ이 추가된 경우
	public static final int	BIEUB			= getCondNum("ㅂ");
	// 가까우리만치 -> 가깝 + 으리만치 로 분석 될 수 있게, ㅂ 불규칙 활용 동사를 '가까우'+'리만치'로 결합하도록
	
	// 자음 탈락 조건
	public static final int	MINUS_LIEUL		= getCondNum("-ㄹ");
	// ㅂ 탈락에 대한 설정
	public static final int	MINUS_BIEUB		= getCondNum("-ㅂ");
	public static final int	MINUS_SIOT		= getCondNum("-ㅅ");
	public static final int	MINUS_HIEUT		= getCondNum("-ㅎ");
	
	// 용언 조건
	public static final int	HA				= getCondNum("하");
	public static final int	GADA			= getCondNum("가다");
	public static final int	ODA				= getCondNum("오다");
	
	public static final int	F				= getCondNum("F");
	public static final int	SHORTEN			= getCondNum("생략");

	public static final int	MINUS_JA_SET	= MINUS_LIEUL | MINUS_SIOT | MINUS_HIEUT;
	public static final int	SET_FOR_UN		= JAEUM | MOEUM | YANGSEONG | EUMSEONG;
}
