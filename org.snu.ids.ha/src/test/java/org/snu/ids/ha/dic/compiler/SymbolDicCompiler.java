package org.snu.ids.ha.dic.compiler;


import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.util.Convert;
import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


public class SymbolDicCompiler
{
	public static void compile(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;
			ArrayList<TypedSymbol> symbolList = new ArrayList<TypedSymbol>();

			int valSize = 0;
			int keyArrSize = 0;
			final int bucketSize = 1000;

			while( (arr = dicReader.read()) != null ) {
				TypedSymbol symbol = new TypedSymbol(arr);
				symbolList.add(symbol);
				keyArrSize += symbol.word.length;
				valSize++;
			}
			Collections.sort(symbolList, new Comparator<TypedSymbol>()
			{
				public int compare(TypedSymbol arg0, TypedSymbol arg1)
				{
					return arg0.getBucketPos(bucketSize) - arg1.getBucketPos(bucketSize);
				}
			});

			// construct hash
			int[] bucket = new int[bucketSize];
			int[] nextPosArr = new int[valSize];
			int[] keyHeadPosArr = new int[valSize];
			char[] keyArr = new char[keyArrSize];
			long[] tagArr = new long[valSize];

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);

			int valPos = 0, bucketPos = 0, curPos = 0, curKeyHeadPos = 0;
			for( TypedSymbol symbol : symbolList ) {
				bucketPos = symbol.getBucketPos(bucketSize);
				valPos = bucket[bucketPos];
				// blank
				if( valPos == -1 ) {
					bucket[bucketPos] = curPos;
				} else {
					while( nextPosArr[valPos] != -1 ) {
						valPos = nextPosArr[valPos];
					}
					nextPosArr[valPos] = curPos;
				}

				// fill value
				keyHeadPosArr[curPos] = curKeyHeadPos;
				System.arraycopy(symbol.word, 0, keyArr, curKeyHeadPos, symbol.word.length);
				tagArr[curPos] = symbol.tag;

				// increase memory position
				curKeyHeadPos += symbol.word.length;
				curPos++;
			}

			byte[] temp = null;

			// write size information
			temp = Convert.toByta(bucketSize);
			fos.write(temp);
			temp = Convert.toByta(valSize);
			fos.write(temp);
			temp = Convert.toByta(keyArrSize);
			fos.write(temp);

			// write hash
			temp = Convert.toByta(bucket);
			fos.write(temp);
			temp = Convert.toByta(nextPosArr);
			fos.write(temp);
			temp = Convert.toByta(keyHeadPosArr);
			fos.write(temp);
			temp = Convert.toByta(keyArr);
			fos.write(temp);
			temp = Convert.toByta(tagArr);
			fos.write(temp);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}
}

class TypedSymbol
{
	char[]	word	= null;
	long	tag		= POSTag.SW;
	int		hash	= 0;


	public TypedSymbol(String[] src)
	{
		this.word = src[0].toCharArray();
		this.tag = POSTag.getTagNum(src[1]);
		this.hash = RSHash.hash(word);
	}


	public int hashCode()
	{
		return hash;
	}


	public int getBucketPos(int bucketSize)
	{
		return Math.abs(hash) % bucketSize;
	}
}