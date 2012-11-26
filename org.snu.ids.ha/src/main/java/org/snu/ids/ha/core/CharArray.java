package org.snu.ids.ha.core;


import org.snu.ids.ha.util.RSHash;


/**
 * <pre>
 * [아][버][지][가][방][에][들][어][가][신][다][.]
 * start: 0, length : 2 면, "아버"를 나타냄.
 * 
 * hashCode와 equals를 이용해서 빠르게 hash에 의한 map을 구현함.
 * </pre>
 * @author 	Dongjoo
 * @since	2011. 7. 14.
 */
public class CharArray
{
	public char[]	array		= null;
	public int		start		= 0;
	public int		length		= 0;
	public int		hashCode	= 0;


	CharArray()
	{
	}


	public CharArray(String str)
	{
		this(str.toCharArray());
	}


	public CharArray(char[] array)
	{
		this(array, 0, array.length);
	}


	public CharArray(char[] array, int start, int length)
	{
		this.array = array;
		this.start = start;
		this.length = length;
		this.hashCode = RSHash.hash(array, start, length);
	}


	public CharArray subCharArray(int start, int length)
	{
		return new CharArray(array, this.start + start, length);
	}


	public CharArray subCharArray(int start)
	{
		return new CharArray(array, this.start + start, length - start);
	}


	public char[] getWord()
	{
		char[] word = new char[length];
		System.arraycopy(array, start, word, 0, length);
		return word;
	}


	public char[] getArray()
	{
		return array;
	}


	public void setCharAt(int idx, char ch)
	{
		array[idx] = ch;
	}


	public char getLastChar()
	{
		return array[start + length - 1];
	}


	@Override
	public int hashCode()
	{
		return hashCode;
	}


	@Override
	public boolean equals(Object obj)
	{
		if( !(obj instanceof CharArray) ) return false;
		CharArray comp = (CharArray) obj;
		if( this.length != comp.length ) return false;
		for( int i = 0; i < this.length; i++ ) {
			if( this.array[start + i] != comp.array[comp.start + i] ) return false;
		}
		return true;
	}


	public String toString()
	{
		return new String(array, start, length);
	}
}
