package org.snu.ids.ha.util;


public class RSHash
{
	public static int hash(String str)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;

		for( int i = 0; i < str.length(); i++ ) {
			hash = hash * a + str.charAt(i);
			a = a * b;
		}

		return hash;
	}


	public static int hash(char[] array)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		for( int i = 0; i < array.length; i++ ) {
			hash = hash * a + array[i];
			a = a * b;
		}
		return hash;
	}


	public static int hash(char[] array, int start, int length)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		for( int offset = start, end = start + length; offset < end; offset++ ) {
			hash = hash * a + array[offset];
			a = a * b;
		}
		return hash;
	}


	public static int hash(char[] array, long tag)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		for( int i = 0; i < array.length; i++ ) {
			hash = hash * a + array[i];
			a = a * b;
		}
		hash = hash * a + (int) tag;
		return hash;
	}


	public static int hash(char[] prevArray, long prevTag, char[] nextWord, long nextTag)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		for( int i = 0; i < prevArray.length; i++ ) {
			hash = hash * a + prevArray[i];
			a = a * b;
		}
		hash = hash * a + (int) prevTag;
		a = a * b;
		for( int i = 0; i < nextWord.length; i++ ) {
			hash = hash * a + nextWord[i];
			a = a * b;
		}
		hash = hash * a + (int) nextTag;
		return hash;
	}


	public static int hash(long prevTag, char[] nextWord, long nextTag)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		hash = hash * a + (int) prevTag;
		a = a * b;
		for( int i = 0; i < nextWord.length; i++ ) {
			hash = hash * a + nextWord[i];
			a = a * b;
		}
		hash = hash * a + (int) nextTag;
		return hash;
	}


	public static int hash(char ch1, char ch2)
	{
		int b = 378551;
		int a = 63689;
		int hash = 0;
		hash = hash * a + ch1;
		a = a * b;
		hash = hash * a + ch2;
		return hash;
	}
}
