package org.snu.ids.ha.core;


import org.snu.ids.ha.constants.POSTag;


public class Keyword
{
	int		start	= 0;
	char[]	word	= null;
	long	tag		= 0l;


	public Keyword(char[] word, long tag, int start)
	{
		this.word = word;
		this.tag = tag;
		this.start = start;
	}


	public char[] getWord()
	{
		return word;
	}


	public int length()
	{
		return word.length;
	}


	public String toString()
	{
		return new String(word) + "/" + POSTag.getTag(tag);
		//return start + "/" + new String(word) + "/" + POSTag.getTag(tag);
	}
}
