package org.snu.ids.ha.core;


public class Token
	extends CharArray
	implements Comparable<Token>
{
	protected CharSetType	charSet	= CharSetType.ETC;


	public Token(char[] array, int start, int length, CharSetType charSet)
	{
		super(array, start, length);
		this.charSet = charSet;
	}


	public CharSetType getCharSet()
	{
		return charSet;
	}


	public void setCharSet(CharSetType charSet)
	{
		this.charSet = charSet;
	}


	public int compareTo(Token tk)
	{
		return start - tk.start;
	}
	
	
	public String toString()
	{
		return "(" + start + "," + super.toString() + "," + charSet + ")";
	}
}
