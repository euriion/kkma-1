package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


public class LnprMorpsGExpDic
{
	private int		bucketSize			= 0;
	private int[]	bucket				= null;
	private int[]	nextPosArr			= null;
	private int[]	prevWordHeadPosArr	= null;
	private int[]	nextWordHeadPosArr	= null;
	private char[]	prevWordArr			= null;
	private char[]	nextWordArr			= null;
	private long[]	prevTagArr			= null;
	private long[]	nextTagArr			= null;
	private float[]	valArr				= null;


	public LnprMorpsGExpDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(ClassLoader.getSystemResourceAsStream(fileName));

			bucketSize = bbr.readInt();
			int valSize = bbr.readInt();
			int prevWordHeadArrSize = bbr.readInt();
			int nextWordHeadArrSize = bbr.readInt();

			bucket = bbr.readIntA(bucketSize);

			nextPosArr = bbr.readIntA(valSize);
			prevWordHeadPosArr = bbr.readIntA(valSize);
			nextWordHeadPosArr = bbr.readIntA(valSize);

			prevWordArr = bbr.readCharA(prevWordHeadArrSize);
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


	/**
	 * <pre>
	 * 기분석 된 표층형에 대한 확률 값을 반환한다.
	 * 만일 기분석 된 결과가 없다면 1을 반환하여, 후처리를 할 수 있도록 한다.
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 7. 19.
	 * @param prevWord
	 * @param prevTag
	 * @param nextWord
	 * @param nextTag
	 * @return
	 */
	public float get(char[] prevWord, long prevTag, char[] nextWord, long nextTag)
	{
		int bucketPos = Math.abs(RSHash.hash(prevWord, prevTag, nextWord, nextTag)) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, prevWord, prevTag, nextWord, nextTag) ) return valArr[valPos];
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, prevWord, prevTag, nextWord, nextTag) ) return valArr[valPos];
			}
		}
		return 1;
	}


	private boolean matched(int valPos, char[] prevWord, long prevTag, char[] nextWord, long nextTag)
	{
		int prevWordHeadPos = prevWordHeadPosArr[valPos], nextWordHeadPos = nextWordHeadPosArr[valPos];
		int prevWordLen = (valPos + 1 < prevWordHeadPosArr.length ? prevWordHeadPosArr[valPos + 1] : prevWordArr.length) - prevWordHeadPos;
		int nextWordLen = (valPos + 1 < nextWordHeadPosArr.length ? nextWordHeadPosArr[valPos + 1] : nextWordArr.length) - nextWordHeadPos;
		if( prevTagArr[valPos] != prevTag ) return false;
		if( nextTagArr[valPos] != nextTag ) return false;
		if( prevWordLen != prevWord.length ) return false;
		if( nextWordLen != nextWord.length ) return false;
		for( int i = 0; i < prevWordLen; i++ ) {
			if( prevWordArr[prevWordHeadPos + i] != prevWord[i] ) return false;
		}
		for( int i = 0; i < nextWordLen; i++ ) {
			if( nextWordArr[nextWordHeadPos + i] != nextWord[i] ) return false;
		}
		return true;
	}
}