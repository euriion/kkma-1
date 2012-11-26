package org.snu.ids.ha.dic;


import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.core.CharArray;
import org.snu.ids.ha.util.Util;


public class ProbDicSet
{
	private static LnprPosDic				lnprPosDic				= null;
	private static LnprPosGPosDic			lnprPosGPosIntraDic		= null;
	private static LnprPosGPosDic			lnprPosGPosInterDic		= null;
	private static LnprMorpDic				lnprMorpDic				= null;
	private static LnprMorpDic				lnprPosGExpDic			= null;
	private static LnprMorpsGExpDic			lnprMorpsGExpDic		= null;
	private static LnprPosGMorpDic			lnprPosGMorpIntraDic	= null;
	private static LnprPosGMorpDic			lnprPosGMorpInterDic	= null;

	private static LnprSyllableUniNounDic	lnprSyllableUniNounDic	= null;
	private static LnprSyllableBiDic		lnprSyllableBiDic		= null;

	static {
		// load from dic root
		//load("D:/Eclipse/workspace/org.snu.ids.ha3/dic");
		load("dic");
	}


	public static void load(String dicRoot)
	{
		lnprPosDic = new LnprPosDic(dicRoot + "/compile_lnpr_pos.dic");
		lnprPosGPosIntraDic = new LnprPosGPosDic(dicRoot + "/compile_lnpr_pos_g_pos_intra.dic");
		lnprPosGPosInterDic = new LnprPosGPosDic(dicRoot + "/compile_lnpr_pos_g_pos_inter.dic");

		lnprMorpDic = new LnprMorpDic(dicRoot + "/compile_lnpr_morp.dic");
		lnprPosGExpDic = new LnprMorpDic(dicRoot + "/compile_lnpr_pos_g_exp.dic");
		lnprMorpsGExpDic = new LnprMorpsGExpDic(dicRoot + "/compile_lnpr_morps_g_exp.dic");
		lnprPosGMorpIntraDic = new LnprPosGMorpDic(dicRoot + "/compile_lnpr_pos_g_morp_intra.dic");
		lnprPosGMorpInterDic = new LnprPosGMorpDic(dicRoot + "/compile_lnpr_pos_g_morp_inter.dic");

		lnprSyllableUniNounDic = new LnprSyllableUniNounDic(dicRoot + "/compile_lnpr_syllable_uni_noun.dic");
		lnprSyllableBiDic = new LnprSyllableBiDic(dicRoot + "/compile_lnpr_syllable_bi.dic");
	}


	public static float getLnprPos(long tag)
	{
		return lnprPosDic.get(getProbTag(tag));
	}


	public static float getLnprPosGPosIntra(long prevTag, long givenTag)
	{
		return lnprPosGPosIntraDic.get(getProbTag(prevTag), getProbTag(givenTag));
	}


	public static float getLnprPosGPosInter(long prevTag, long givenTag)
	{
		return lnprPosGPosInterDic.get(getProbTag(prevTag), getProbTag(givenTag));
	}


	public static float getLnprMorp(char[] word, long tag)
	{
		return lnprMorpDic.get(word, getProbTag(tag));
	}


	public static float getLnprPosGExp(char[] exp, long tag)
	{
		long probTag = getProbTag(tag);
		float lnprPosGExp = lnprPosGExpDic.get(exp, probTag);
		if( lnprPosGExp == -18f ) {
			if( POSTag.isTagOf(tag, POSTag.ETC) ) {
				lnprPosGExp = lnprPosDic.get(probTag);
			} else if( POSTag.isTagOf(probTag, POSTag.NNA) ) {
				lnprPosGExp = getLnprNoun(exp);
			} else {
				lnprPosGExp = lnprPosDic.get(probTag);
			}
		}
		return lnprPosGExp;
	}


	public static float getLnprMorpsGExp(char[] prevWord, long prevTag, char[] nextWord, long nextTag)
	{
		return lnprMorpsGExpDic.get(prevWord, getProbTag2(prevTag), nextWord, getProbTag2(nextTag));
	}


	public static float getLnprPosGMorpIntra(long prevTag, char[] nextWord, long nextTag)
	{
		float lnpr = lnprPosGMorpIntraDic.get(getProbTag(prevTag), nextWord, getProbTag(nextTag));
		if( lnpr == LnprPosGMorpDic.MIN_LNPR_MORP && (getLnprMorp(nextWord, nextTag) < -14f || POSTag.isTagOf(prevTag, POSTag.S | POSTag.NNP)) ) {
			lnpr = getLnprPosGPosIntra(prevTag, nextTag);
		}
		return lnpr;
	}


	/**
	 * <pre>
	 * 주어진 형태소에 대한 이전 형태소 품사의 어절간 출현 확률 반환
	 *    찾아지지 않는 경우에 대해서는 근사화 하여 반환 
	 * P(prevTag|word, tag)
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 25.
	 * @param prevTag
	 * @param nextWord
	 * @param nextTag
	 * @return
	 */
	public static float getLnprPosGMorpInter(long prevTag, char[] nextWord, long nextTag)
	{
		float lnpr = lnprPosGMorpInterDic.get(getProbTag(prevTag), nextWord, getProbTag(nextTag));
		if( lnpr == LnprPosGMorpDic.MIN_LNPR_MORP ) {
			float lnprMorp = getLnprMorp(nextWord, nextTag);
			if( lnprMorp < -14f ) {
				lnpr = getLnprPosGPosInter(prevTag, nextTag);
			} else {
				lnpr = -17.4f - lnprMorp;
			}
		}
		return lnpr;
	}


	public static float getLnprSyllableBi(char ch1, char ch2, boolean isSpacing)
	{
		return lnprSyllableBiDic.get(ch1, ch2, isSpacing);
	}


	private static long getProbTag(long tag)
	{
		if( (tag & (POSTag.NNA | POSTag.UN | POSTag.OL | POSTag.OH)) > 0 ) {
			return POSTag.NNA;
		} else if( (tag & POSTag.VX) > 0 ) {
			return POSTag.VX;
		} else if( (tag & POSTag.MM) > 0 ) {
			return POSTag.MM;
		} else if( (tag & POSTag.EP) > 0 ) {
			return POSTag.EP;
		} else if( (tag & POSTag.EF) > 0 ) {
			return POSTag.EF;
		} else if( tag == POSTag.ON ) {
			return POSTag.NR;
		} else if( tag == POSTag.NNM ) {
			return POSTag.NNB;
		}
		return tag;
	}


	private static long getProbTag2(long tag)
	{
		if( (tag & POSTag.VX) > 0 ) {
			return POSTag.VX;
		} else if( (tag & POSTag.MM) > 0 ) {
			return POSTag.MM;
		} else if( (tag & POSTag.EP) > 0 ) {
			return POSTag.EP;
		} else if( (tag & POSTag.EF) > 0 ) {
			return POSTag.EF;
		} else if( tag == POSTag.NNM ) {
			return POSTag.NNB;
		}
		return tag;
	}


	/**
	 * <pre>
	 * 해당 문자열에 대한 띄어쓰기 확률 값을 반환한다.
	 * 주어진 문자열은 한글 문자열이나 공백으로 이루어졌다고 가정한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2009. 12. 11
	 * @param str
	 * @return
	 */
	public static float getLnprOfSpacing(String str)
	{
		if( !Util.valid(str) ) return Integer.MIN_VALUE;
		float prob = 0;
		str = str.trim().replaceAll("[ \t]+", " ");

		for( int i = 0, len = str.length() - 1; i < len; i++ ) {
			boolean hasSpace = false;
			char ch1 = str.charAt(i);
			char ch2 = str.charAt(i + 1);
			if( ch2 == ' ' ) {
				ch2 = str.charAt(i + 1);
				i++;
				hasSpace = true;
			}
			prob += getLnprSyllableBi(ch1, ch2, hasSpace);
		}

		return prob;
	}


	/**
	 * <pre>
	 * 띄어쓰기를 포함하지 않은 문자열에 대한 띄어쓰기 태깅 확률을 반환함.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 20.
	 * @param charArray
	 * @return
	 */
	public static float getLnprOfSpacing(CharArray charArray)
	{
		float lnpr = 0;
		for( int i = 1; i < charArray.length; i++ ) {
			lnpr += getLnprSyllableBi(charArray.array[charArray.start + i - 1], charArray.array[charArray.start + i], false);
		}
		return lnpr;
	}


	public static float getLnprOfSpacing(char[] word)
	{
		float lnpr = 0;
		for( int i = 1; i < word.length; i++ ) {
			lnpr += getLnprSyllableBi(word[i - 1], word[i], false);
		}
		return lnpr;
	}


	/**
	 * <pre>
	 * 음절간 출현이 독립적이라 보고 
	 * P(prevTag|abs) = P(prevTag|a)P(prevTag|b)P(prevTag|c)/P(prevTag)P(prevTag)
	 *            = P(prevTag|a)P(prevTag|b)P(prevTag|c)로 처리해서 반환
	 * P(prevTag)P(prevTag) 부분을 생략하여 신조어에 대한 일종의 패널티로 작용하도록 함. 
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 4. 2.
	 * @param array
	 * @return
	 */
	public static float getLnprNoun(char[] array)
	{
		float prob = 0;
		for( int i = 0, len = array.length; i < len; i++ ) {
			prob += lnprSyllableUniNounDic.get(array[i]);
			prob += -0.5f;
		}
		return prob;
	}


	/**
	 * <pre>
	 * 명사에 대한 품사 태깅 추정 확률을 구함.
	 * 사전에 등록된 명사에 한하여 추정하는 것으로 페널티를 적용하지 않아서 확률 값이 적당히 크게 나오게 함.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 4. 26.
	 * @param str
	 * @return
	 */
	public static float getLnprNoun2(char[] str)
	{
		float prob = 0;
		for( int i = 0, len = str.length; i < len; i++ ) {
			prob += lnprSyllableUniNounDic.get(str[i]);
			if( i > 0 ) prob -= getLnprPos(POSTag.NNA);
		}
		return prob;
	}


	public static float getLnprOfTagging(char[][] wordArr, long[] infoEncArr)
	{
		float lnpr = 0;
		boolean withSpace = false;
		char[] prevWord = null, curWord = null;
		long prevTag = 0l, curTag = 0l;
		for( int i = 0; i < wordArr.length; i++ ) {
			curWord = wordArr[i];
			curTag = (POSTag.MASK_TAG & infoEncArr[i]);

			if( curWord[0] == ' ' ) {
				withSpace = true;
				continue;
			}

			float lnprPosGExp = getLnprPosGExp(curWord, curTag);

			//System.out.println(new String(curWord) + "/" + POSTag.getTag(curTag) + "\t" + lnprPosGExp);

			if( i > 0 ) {
				float temp = getLnprMorpsGExp(prevWord, prevTag, curWord, curTag);
				//System.out.println("\t" + new String(prevWord) + "/" + POSTag.getTag(prevTag) + "+" + new String(curWord) + "/" + POSTag.getTag(curTag) + "\t" + temp);
				if( temp <= 0 ) {
					lnpr += temp - lnprPosGExp;
				} else {
					if( withSpace ) {
						lnpr += getLnprPosGMorpInter(prevTag, curWord, curTag);
						//System.out.println("\tINTER " + POSTag.getTag(prevTag) + "+" + new String(curWord) + "/" + POSTag.getTag(curTag) + "\t" + getLnprPosGMorpInter(prevTag, curWord, curTag));
					} else {
						lnpr += getLnprPosGMorpIntra(prevTag, curWord, curTag);
						//System.out.println("\tINTRA " + POSTag.getTag(prevTag) + "+" + new String(curWord) + "/" + POSTag.getTag(curTag) + "\t" + getLnprPosGMorpIntra(prevTag, curWord, curTag));
					}
				}
			}

			lnpr += lnprPosGExp;

			prevWord = curWord;
			prevTag = curTag;
			withSpace = false;
		}

		return lnpr;
	}


	public static float getLnprOfTagging(String src)
	{
		String[] arr = src.split("[+]");
		char[][] wordArr = new char[arr.length][];
		long[] infoEncArr = new long[arr.length];

		for( int i = 0; i < arr.length; i++ ) {
			String[] temp = arr[i].split("/");
			wordArr[i] = temp[0].toCharArray();
			if( wordArr[i][0] == ' ' ) {
				infoEncArr[i] = POSTag.SW;
			} else {
				infoEncArr[i] = POSTag.getTagNum(temp[1]);
			}
		}
		return getLnprOfTagging(wordArr, infoEncArr);
	}


	static float getLnprMorp(String word, long tag)
	{
		return getLnprMorp(word.toCharArray(), tag);
	}


	static float getLnprNoun(String word)
	{
		return getLnprNoun(word.toCharArray());
	}


	public static void main(String[] args)
	{
		System.out.println(ProbDicSet.getLnprOfTagging("내/NP+가/JKS+ +만일/NNG+ +안철수/UN+이/VCP+라면/EC"));
		System.out.println(ProbDicSet.getLnprOfTagging("내/NP+가/JKS+ +만일/NNG+ +안/NNG+철/NNG+수/NNG+이/VCP+라면/EC"));
	}
}
