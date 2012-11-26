package org.snu.ids.ha.test;


import java.util.ArrayList;
import java.util.List;

import org.snu.ids.ha.constants.POSTag;


public class ANSEojeol
	extends ArrayList<ANSMorp>
{
	String	exp	= null;


	public ANSEojeol()
	{
		super();
	}


	/**
	 * @return the exp
	 */
	public String getExp()
	{
		return exp;
	}


	/**
	 * @param exp the exp to set
	 */
	public void setExp(String exp)
	{
		this.exp = exp;
	}


	void merge()
	{
		List<ANSMorp> mpl = new ArrayList<ANSMorp>();
		ANSMorp mpPrev = null, mp = null;
		for( int i = 0, size = size(); i < size; i++ ) {
			mp = get(i);

			if( mpPrev != null ) {
				// 용언 접두사 결합
				if( mpPrev.isTagOf(POSTag.XPV) ) {
					mpl.remove(mpl.size() - 1);
					mp.word = mpPrev.word + mp.word;
				}
				// 명사형 접두사 결합
				else if( mpPrev.isTagOf(POSTag.XPN) ) {
					mpl.remove(mpl.size() - 1);
					mp.word = mpPrev.word + mp.word;
				}
				// 명사형 접미사 결합
				else if( mp.isTagOf(POSTag.XSN) ) {
					mpPrev.word = mpPrev.word + mp.word;
					mpPrev.tagNum = POSTag.NNG;
					continue;
				}
				// 형용사형 접미사 결합
				else if( mp.isTagOf(POSTag.XSA) ) {
					mpPrev.word = mpPrev.word + mp.word;
					mpPrev.tagNum = POSTag.VA;
					continue;
				}
				// 동사형 접미사 결합
				else if( mp.isTagOf(POSTag.XSV) ) {
					mpPrev.word = mpPrev.word + mp.word;
					mpPrev.tagNum = POSTag.VV;
					continue;
				}
			}

			mpPrev = mp;
			mpl.add(mp);
		}
		this.clear();
		this.addAll(mpl);
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for( ANSMorp mp : this ) {
			if( !isFirst ) {
				sb.append("+");
			}
			sb.append(mp);
			isFirst = false;
		}
		return sb.toString();
	}
}
