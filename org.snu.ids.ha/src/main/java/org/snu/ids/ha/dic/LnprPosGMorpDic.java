package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


public class LnprPosGMorpDic
{
	public static final float	MIN_LNPR_MORP		= -18f;

	private int					bucketSize			= 0;
	private int[]				bucket				= null;
	private int[]				nextPosArr			= null;
	private int[]				nextWordHeadPosArr	= null;
	private char[]				nextWordArr			= null;
	private long[]				prevTagArr			= null;
	private long[]				nextTagArr			= null;
	private float[]				valArr				= null;


	public LnprPosGMorpDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));

			bucketSize = bbr.readInt();
			int valSize = bbr.readInt();
			int nextWordHeadArrSize = bbr.readInt();

			bucket = bbr.readIntA(bucketSize);
			nextPosArr = bbr.readIntA(valSize);
			nextWordHeadPosArr = bbr.readIntA(valSize);
			nextWordArr = bbr.readCharA(nextWordHeadArrSize);
			prevTagArr = bbr.readLongA(valSize);
			nextTagArr = bbr.readLongA(valSize);
			valArr = bbr.readFloatA(valSize);

			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(long prevTag, char[] nextWord, long nextTag)
	{
		int bucketPos = Math.abs(RSHash.hash(prevTag, nextWord, nextTag)) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, prevTag, nextWord, nextTag) ) return valArr[valPos];
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, prevTag, nextWord, nextTag) ) return valArr[valPos];
			}
		}
		return MIN_LNPR_MORP;
	}


	private boolean matched(int valPos, long prevTag, char[] nextWord, long nextTag)
	{
		int nextWordHeadPos = nextWordHeadPosArr[valPos];
		int nextWordLen = (valPos + 1 < nextWordHeadPosArr.length ? nextWordHeadPosArr[valPos + 1] : nextWordArr.length) - nextWordHeadPos;
		if( prevTagArr[valPos] != prevTag ) return false;
		if( nextTagArr[valPos] != nextTag ) return false;
		if( nextWordLen != nextWord.length ) return false;
		for( int i = 0; i < nextWordLen; i++ ) {
			if( nextWordArr[nextWordHeadPos + i] != nextWord[i] ) return false;
		}
		return true;
	}
}
