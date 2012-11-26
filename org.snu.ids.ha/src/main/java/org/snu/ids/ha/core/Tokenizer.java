package org.snu.ids.ha.core;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer
{
	public static final TokenPattern[]	PREDEFINED_TOKEN_PATTERN	= {
		// number
		new TokenPattern("[-]?[0-9]+([,][0-9]{3})*([.][0-9]+)?", CharSetType.NUMBER),
		// URL pattern
		new TokenPattern("[hH][tT][tT][pP]([sS]*)://[a-zA-Z0-9/_.?=%&\\-]+", CharSetType.COMBINED),
		// model name pattern
		new TokenPattern("[a-zA-Z0-9]+[-][a-zA-Z0-9]+", CharSetType.COMBINED),
//		// ㅋㅋㅋ, ㅠㅠㅠㅠㅠ, ㅜㅜㅜㅜㅜ와 같은 단순 문자 반복 이모티콘
//		new TokenPattern("(ㅋ|ㅠ|ㅜ|ㅎ){2,}", CharSetType.EMOTICON), 
//		new TokenPattern("(히|흐|크|키|케|캬){2,}", CharSetType.EMOTICON), 
//		new TokenPattern("(\\^){3,}", CharSetType.EMOTICON),
//		// 웃는 이모티콘
//		// (^_^'), ^_^', d^_^b, d-_-b, (^_^), ^_^, (^-^), ^-^, (^ ^), ^^, (^.^), ^.^v, ^.^V, ^.^, (^o^), ^o^, (^3^), ^3^, ^_^", ^_^;, ^^;, ^^a
//		new TokenPattern("[(][\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^][']?[)]", CharSetType.EMOTICON), 
//		new TokenPattern("[d][\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^][b]", CharSetType.EMOTICON), 
//		new TokenPattern("[\\^]([.]|_|[-]|o|0|O|3|~|[ ])?[\\^]([;]+|['\"avVㅗ])?", CharSetType.EMOTICON),
//		// 우는 이모티콘
//		new TokenPattern("[(];_;[)]", CharSetType.EMOTICON), 
//		new TokenPattern("[(]T[_.~oO\\^]?T[)]", CharSetType.EMOTICON), 
//		new TokenPattern("ㅜ[_.]?ㅜ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅡ[_.]?ㅜ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅜ[_.]?ㅡ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅠ[_.]?ㅠ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅡ[_.]?ㅠ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅠ[_.]?ㅡ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅠ[_.]?ㅜ", CharSetType.EMOTICON), 
//		new TokenPattern("ㅜ[_.]?ㅠ", CharSetType.EMOTICON),
//		// 인상 찡그린 이모티콘
//		// (-.-), -_-;, -_-a, -_-ㅗ, --ㅗ, -.-, -_-, (-.-)zzZ, -_-zzZ
//		new TokenPattern("[(][-](_|[.])?[-]([;]+|[aㅗ])?[)](zzZ)?", CharSetType.EMOTICON), 
//		new TokenPattern("[-](_|[.])?[-]([;]+|[aㅗ]|(zzZ))?", CharSetType.EMOTICON), 
//		new TokenPattern("[ㅡ](_|[.])?[ㅡ]([;]+|[aㅗ]|(zzZ))?", CharSetType.EMOTICON),
//		// (>_<), >_<, (>.<), >.<, (>_>), >_>, (¬_¬)
//		new TokenPattern("[(][>]([.]|_)?[<][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[>]([.]|_)?[<]", CharSetType.EMOTICON), 
//		new TokenPattern("[(][>]([.]|_)?[>][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[>]([.]|_){1}[>]", CharSetType.EMOTICON), 
//		new TokenPattern("[(][¬]([.]|_)?[¬][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[¬]([.]|_)?[¬]", CharSetType.EMOTICON),
//		// 윙크 이모티콘
//		// (`_^), `_^, (^_~), ^_~, ~.^, ^.~
//		new TokenPattern("[(]'(_|[.])\\^[)]", CharSetType.EMOTICON), 
//		new TokenPattern("'(_|[.])\\^", CharSetType.EMOTICON), 
//		new TokenPattern("\\^(_|[.])[~]", CharSetType.EMOTICON), 
//		new TokenPattern("[~](_|[.])\\^", CharSetType.EMOTICON),
//		// 띠옹띠옹
//		// (._.), (,_,), (X_X), 0.o, O_o
//		new TokenPattern("[(][.][_][.][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[(]['][_]['][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[(][,][_][,][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[(][X][_][X][)]", CharSetType.EMOTICON), 
//		new TokenPattern("[O][_.][o]", CharSetType.EMOTICON), 
//		new TokenPattern("[o][_.][O]", CharSetType.EMOTICON),
//		// 절
//		new TokenPattern("m[(]_ _[)]m", CharSetType.EMOTICON) 
		};


	public static List<Token> tokenize(CharArray charArray)
	{
		// init result
		ArrayList<Token> tkList = new ArrayList<Token>();

		// init char array
		char[] array = charArray.array;

		// copy char arrayCpy for pattern matching
		CharArraySequence charArrayCpy = new CharArraySequence(array);

		for( int i = 0, ptnlen = PREDEFINED_TOKEN_PATTERN.length; i < ptnlen; i++ ) {
			TokenPattern tkptn = PREDEFINED_TOKEN_PATTERN[i];
			tkList.addAll(find(charArrayCpy, tkptn));
		}

		int strlen = array.length;
		boolean[] chkPrednfdPtn = checkFound(strlen, tkList);

		char preCh = 0, ch = 0;
		CharSetType curCharSet = CharSetType.ETC, prevCharSet = CharSetType.ETC;
		int start = 0;

		for( int i = 0; i < strlen; i++ ) {
			ch = array[i];
			prevCharSet = curCharSet;
			Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);

			// 이모티콘 확인
			if( chkPrednfdPtn[i] ) {
				curCharSet = CharSetType.EMOTICON;
			}
			// 이모티콘 아닌 경우 확인
			else if( ub == Character.UnicodeBlock.HANGUL_SYLLABLES || ub == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO ) {
				curCharSet = CharSetType.HANGUL;
			} else if( ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ) {
				curCharSet = CharSetType.HANMUN;
			} else if( (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') ) {
				curCharSet = CharSetType.ENGLISH;
			} else if( ch >= '0' && ch <= '9' ) {
				curCharSet = CharSetType.NUMBER;
			} else if( ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' ) {
				curCharSet = CharSetType.SPACE;
			} else if( ub == Character.UnicodeBlock.LETTERLIKE_SYMBOLS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS || ub == Character.UnicodeBlock.BASIC_LATIN ) {
				curCharSet = CharSetType.SYMBOL;
			} else {
				curCharSet = CharSetType.ETC;
			}

			// token 추출
			if( i != 0
			// 문자 집합이 변화한 경우 
			&& (prevCharSet != curCharSet
			// 기타 문자 집합이고 동일한 문자가 연속되지 않은 경우
			|| (curCharSet == CharSetType.ETC && preCh != ch)
			// 기호이며 동일한 기호가 연속되지 않은 경우
			|| (curCharSet == CharSetType.SYMBOL && preCh != ch)) ) {
				// 이미 추출된 패턴은 따로 추출함.
				if( prevCharSet != CharSetType.EMOTICON ) {
					tkList.add(new Token(array, start, i - start, prevCharSet));
				}
				start = i;
			}
			preCh = ch;

		}//length for i

		// 마지막 토큰 추출
		if( start < strlen && curCharSet != CharSetType.EMOTICON ) {
			tkList.add(new Token(array, start, strlen - start, curCharSet));
		}

		Collections.sort(tkList);

		return tkList;
	}


	/**
	 * <pre>
	 * 특정 패턴을 토큰으로 생성하여 반환한다.
	 * 토큰이 찾아진 곳은 공백으로 바꾸어서 반환한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2009. 10. 22
	 * @param cas	패턴을 찾을 대상 문자열
	 * @param tkptn	찾을 패턴
	 * @return
	 */
	private static List<Token> find(CharArraySequence cas, TokenPattern tkptn)
	{
		if( tkptn == null ) return null;
		ArrayList<Token> tkList = new ArrayList<Token>();

		Matcher matcher = tkptn.pattern.matcher(cas);
		while( matcher.find() ) {
			tkList.add(new Token(cas.array, matcher.start(), matcher.end() - matcher.start(), tkptn.charSetType));
			for( int i = matcher.start(), end = matcher.end(); i < end; i++ ) {
				cas.setCharAt(i, ' ');
			}
		}
		return tkList;
	}


	/**
	 * <pre>
	 * 문자열에서 토큰이 찾아진 위치를 이미 찾아졌다고 설정.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2009. 10. 22
	 * @param strlen 주어진 문자열의 길이.
	 * @param tkList 이미 찾아진 토큰 목록
	 * @return
	 */
	private static boolean[] checkFound(int strlen, List<Token> tkList)
	{
		boolean[] bFound = new boolean[strlen];
		for( int i = 0; i < strlen; i++ )
			bFound[i] = false;

		for( Token tk : tkList ) {
			for( int i = 0; i < tk.length; i++ ) {
				bFound[tk.start + i] = true;
			}
		}
		return bFound;
	}
}

class TokenPattern
{
	Pattern		pattern		= null;
	CharSetType	charSetType	= null;


	TokenPattern(String strPattern, CharSetType charSetType)
	{
		pattern = Pattern.compile(strPattern);
		this.charSetType = charSetType;
	}
}

class CharArraySequence
	implements CharSequence
{
	char[]	array		= null;
	char[]	arrayCpy	= null;


	public CharArraySequence(char[] array)
	{
		this.array = array;
		this.arrayCpy = new char[array.length];
		System.arraycopy(array, 0, arrayCpy, 0, array.length);
	}


	public int length()
	{
		return arrayCpy.length;
	}


	public char charAt(int index)
	{
		return arrayCpy[index];
	}


	public void setCharAt(int i, char ch)
	{
		arrayCpy[i] = ch;
	}


	public CharArraySequence subSequence(int start, int end)
	{
		int len = end - start;
		char[] mem = new char[len];
		System.arraycopy(arrayCpy, start, mem, 0, len);
		return new CharArraySequence(mem);
	}
}
