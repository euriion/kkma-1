package org.snu.ids.ha.util;


public class Hangul
{
	public char	cho		= 0;
	public char	jung	= 0;
	public char	jong	= 0;


	public String toString()
	{
		return "(" + cho + "," + jung + "," + jong + ")";
	}


	public boolean hasCho()
	{
		return cho != 0;
	}


	public boolean hasJung()
	{
		return jung != 0;
	}


	public boolean hasJong()
	{
		return jong != 0;
	}


	/**
	 * <pre>
	 * 모음의 양성, 음성, 중성 여부를 저장함
	 * 1	양성	ㅏ, ㅐ, ㅑ, ㅒ, ㅗ, ㅛ, ㅘ
	 * -1	음성	ㅓ, ㅔ, ㅕ, ㅖ, ㅜ, ㅠ, ㅝ, ㅞ, ㅟ, ㅚ, ㅙ
	 * 0	중성	ㅡ, ㅣ, ㅢ
	 * mo	ㅏ, ㅐ, ㅑ, ㅒ, ㅓ, ㅔ, ㅕ, ㅖ, ㅗ, ㅘ, ㅙ, ㅚ, ㅛ, ㅜ, ㅝ, ㅞ, ㅟ, ㅠ, ㅡ, ㅢ, ㅣ
	 * </pre>
	 * @since	2011. 8. 4.
	 * @author	Dongjoo
	 */
	static final short[]	MOR_TYPE_ARR	= { 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, -1, -1, 1, -1, -1, -1, -1, -1, 0, 0, 0 };

	/**
	 * <pre>
	 * 이중 모음 저장
	 * </pre>
	 * @since	2011. 8. 4.
	 * @author	Dongjoo
	 */
	static final boolean[]	MO_DOUBLE_ARR	= { false, false, false, false, false, false, false, false, false, true, true, true, false, false, true, true, true, false, false, false, false };


	public boolean isDoubleMo()
	{
		if( jung >= 'ㅏ' && jung <= 'ㅣ' ) return MO_DOUBLE_ARR[jung % 12623];
		return false;
	}


	public boolean isPositiveMo()
	{
		if( jung >= 'ㅏ' && jung <= 'ㅣ' ) return MOR_TYPE_ARR[jung % 12623] > 0;
		return false;
	}


	public boolean isNegativeMo()
	{
		if( jung >= 'ㅏ' && jung <= 'ㅣ' ) return MOR_TYPE_ARR[jung % 12623] < 0;
		return false;
	}


	public boolean isNeutralMo()
	{
		if( jung >= 'ㅏ' && jung <= 'ㅣ' ) return MOR_TYPE_ARR[jung % 12623] == 0;
		return false;
	}


	static final char[]	CHO_ARR			= { 'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };
	static final char[]	JONG_ARR		= { 0, 'ㄱ', 'ㄲ', 'ㄳ', 'ㄴ', 'ㄵ', 'ㄶ', 'ㄷ', 'ㄹ', 'ㄺ', 'ㄻ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㄿ', 'ㅀ', 'ㅁ', 'ㅂ', 'ㅄ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ' };
	static final char[]	JUNG_ARR		= { 'ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ' };
	/**
	 * mode 값 : 12593
	 * 자음	ㄱ, 	ㄲ,	ㄳ,	ㄴ,	ㄵ,	ㄶ,	ㄷ,	ㄸ,	ㄹ,	ㄺ,	ㄻ,	ㄼ,	ㄽ,	ㄾ,	ㄿ,	ㅀ,	ㅁ,	ㅂ,	ㅃ,	ㅄ,	ㅅ,	ㅆ,	ㅇ,	ㅈ,	ㅉ,	ㅊ,	ㅋ,	ㅌ,	ㅍ,	ㅎ
	 * Idx	0,	1,	2,	3,	4,	5,	6,	7,	8,	9,	10,	11,	12,	13,	14,	15,	16,	17,	18,	19,	20,	21,	22,	23,	24,	25,	26,	27,	28,	29
	 * cid	0,	1,	-1,	2,	-1,	-1,	3,	4,	5,	-1,	-1,	-1,	-1,	-1,	-1,	-1,	6,	7,	8,	-1,	9,	10,	11,	12,	13,	14,	15,	16,	17,	18
	 * jid	1,	2,	3,	4,	5,	6,	7,	-1,	8,	9,	10,	11,	12,	13,	14,	15,	16,	17,	-1,	18,	19,	20,	21,	22,	-1,	23,	24,	25,	26,	27
	 */
	static final int[]	CHO_IDX_ARR		= { 0, 1, -1, 2, -1, -1, 3, 4, 5, -1, -1, -1, -1, -1, -1, -1, 6, 7, 8, -1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };
	static final int[]	JONG_IDX_ARR	= { 1, 2, 3, 4, 5, 6, 7, -1, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, -1, 18, 19, 20, 21, 22, -1, 23, 24, 25, 26, 27 };


	protected static final char getCho(int idx)
	{
		if( idx >= 0 && idx <= 18 ) return CHO_ARR[idx];
		return 0;
	}


	/**
	 * <pre>
	 * 초성에 대한 index를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 22
	 * @param ch
	 * @return
	 */
	protected static final int getChoIdx(char ch)
	{
		if( ch >= 'ㄱ' && ch <= 'ㅎ' ) return CHO_IDX_ARR[ch % 12593];
		return -1;
	}


	protected static final char getJung(int idx)
	{
		return JUNG_ARR[idx];
	}


	/**
	 * <pre>
	 * 중성에 대한 index를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 22
	 * @param ch
	 * @return
	 */
	protected static final int getJungIdx(char ch)
	{
		if( ch >= 'ㅏ' && ch <= 'ㅣ' ) return ch % 12623;
		return -1;
	}


	protected static final char getJong(int idx)
	{
		if( idx >= 0 && idx <= 27 ) return JONG_ARR[idx];
		return 0;
	}


	/**
	 * <pre>
	 * 종성에 대한 index를 반환한다.
	 * </pre>
	 * @author	therocks
	 * @since	2007. 7. 22
	 * @param ch
	 * @return
	 */
	protected static final int getJongIdx(char ch)
	{
		if( ch == 0 || ch == ' ' ) return 0;
		if( ch >= 'ㄱ' && ch <= 'ㅎ' ) return JONG_IDX_ARR[ch % 12593];
		return -1;
	}


	/**
	 * <pre>
	 *
	 * </pre>
	 * @author Pilho Kim [phkim@cluecom.co.kr]
	 * @since	2001. 04. 20
	 * @param ch
	 * @return
	 */
	public static Hangul split(char ch)
	{
		Hangul hangul = new Hangul();
		int x = (ch & 0xFFFF), y = 0, z = 0;
		if( x >= 0xAC00 && x <= 0xD7A3 ) {
			y = x - 0xAC00;
			z = y % (21 * 28);
			hangul.cho = getCho(y / (21 * 28));
			hangul.jung = getJung(z / 28);
			hangul.jong = getJong(z % 28);
		} else if( x >= 0x3131 && x <= 0x3163 ) {
			if( getChoIdx(ch) > -1 ) {
				hangul.cho = ch;
			} else if( getJungIdx(ch) > -1 ) {
				hangul.jung = ch;
			} else if( getJongIdx(ch) > -1 ) {
				hangul.jong = ch;
			}
		} else {
			hangul.cho = ch;
		}
		return hangul;
	}


	/**
	 * <pre>
	 * 초성 중성 종성을 읽어들여서 한글자로 합친다.
	 * </pre>
	 * @author Pilho Kim [phkim@cluecom.co.kr]
	 * @since	2001. 04. 20
	 * @param cho	초성
	 * @param jung	중성
	 * @param jong	종성
	 * @return
	 */
	public static char combine(char cho, char jung, char jong)
	{
		return (char) (getChoIdx(cho) * 21 * 28 + getJungIdx(jung) * 28 + getJongIdx(jong) + 0xAC00);
	}
}