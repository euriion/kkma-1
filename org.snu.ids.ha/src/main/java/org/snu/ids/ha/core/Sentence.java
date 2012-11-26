package org.snu.ids.ha.core;


import java.util.ArrayList;
import java.util.List;

import org.snu.ids.ha.constants.POSTag;


public class Sentence
	extends ArrayList<MCandidate>
{

	public String getSentence()
	{
		MCandidate mcFirst = get(0), mcLast = get(size() - 1);
		char[] array = mcFirst.array;
		return new String(array, mcFirst.start, mcLast.start + mcLast.length - mcFirst.start);
	}


	/**
	 * <pre>
	 * 
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 26.
	 * @param op	1 - 보통명사, 고유명사
	 * 				2 - 조사, 어미 생략
	 * 				3 - 모두 포함
	 * @return
	 */
	public List<Keyword> getKeywords(int op)
	{
		ArrayList<Keyword> ret = new ArrayList<Keyword>();
		for( MCandidate mc : this ) {
			int len = 0;
			for( int i = 0, size = mc.size(); i < size; i++ ) {
				char[] word = mc.getWordAt(i);
				long tag = mc.getTagNumAt(i);

				if( op == 1 && POSTag.isTagOf(tag, POSTag.N) ) {
					Keyword keyword = new Keyword(word, tag, mc.start + len);
					ret.add(keyword);
				} else if( op == 2 && !POSTag.isTagOf(tag, POSTag.J | POSTag.E | POSTag.S) ) {
					Keyword keyword = new Keyword(word, tag, mc.start + len);
					ret.add(keyword);
				} else if( op == 3 ) {
					Keyword keyword = new Keyword(word, tag, mc.start + len);
					ret.add(keyword);
				}

				len += word.length;
			}

			// 복합 명사
			if( mc.compNounArr != null ) {
				len = 0;
				for( int i = 0, size = mc.compNounArr.length; i < size; i++ ) {
					Keyword keyword = new Keyword(mc.compNounArr[i], POSTag.NNG, mc.start + len);
					ret.add(keyword);
					len += mc.compNounArr[i].length;
				}
			}
		}
		return ret;
	}


	public String toString()
	{
		boolean isFirst = true;
		StringBuffer sb = new StringBuffer();
		for( MCandidate mc : this ) {
			if( !isFirst && mc.isWithSpace() ) {
				sb.append(" ");
			} else if( !isFirst ) {
				sb.append("+");
			}
			sb.append(mc.getMorpStr());
			isFirst = false;
		}
		return sb.toString();
	}
}
