package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


public class LnprMorpDic
{
	private static final float	MIN_LNPR_MORP	= -18f;

	private int					bucketSize		= 0;
	private int[]				bucket			= null;
	private int[]				nextPosArr		= null;
	private int[]				keyHeadPosArr	= null;
	private char[]				keyArr			= null;
	private long[]				tagArr			= null;
	private float[]				valArr			= null;


	public LnprMorpDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));

			bucketSize = bbr.readInt();
			int valSize = bbr.readInt();
			int keyArrSize = bbr.readInt();

			bucket = bbr.readIntA(bucketSize);
			nextPosArr = bbr.readIntA(valSize);
			keyHeadPosArr = bbr.readIntA(valSize);
			keyArr = bbr.readCharA(keyArrSize);
			tagArr = bbr.readLongA(valSize);
			valArr = bbr.readFloatA(valSize);

			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(char[] word, long tag)
	{
		int bucketPos = Math.abs(RSHash.hash(word, tag)) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, word, tag) ) return valArr[valPos];
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, word, tag) ) return valArr[valPos];
			}
		}
		return MIN_LNPR_MORP;
	}


	private boolean matched(int valPos, char[] word, long tag)
	{
		int keyHeadPos = keyHeadPosArr[valPos];
		int keyLen = (valPos + 1 < keyHeadPosArr.length ? keyHeadPosArr[valPos + 1] : keyArr.length) - keyHeadPos;
		if( tagArr[valPos] != tag ) return false;
		if( keyLen != word.length ) return false;
		for( int i = 0; i < keyLen; i++ ) {
			if( keyArr[keyHeadPos + i] != word[i] ) return false;
		}
		return true;
	}
}
