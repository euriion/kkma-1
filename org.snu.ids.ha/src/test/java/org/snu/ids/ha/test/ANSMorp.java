package org.snu.ids.ha.test;


import org.snu.ids.ha.constants.POSTag;


public class ANSMorp
{
	String	word	= null;
	long	tagNum	= 0l;


	public ANSMorp(String word, long tagNum)
	{
		this.word = word;
		this.tagNum = tagNum;
	}


	public boolean isTagOf(long tagNums)
	{
		return POSTag.isTagOf(tagNum, tagNums);
	}


	public String toString()
	{
		return word + "/" + POSTag.getTag(tagNum);
	}
}
