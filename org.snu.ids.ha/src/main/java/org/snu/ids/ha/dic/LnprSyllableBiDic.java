package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


public class LnprSyllableBiDic
{
	private static final float	DEFAULT_PROB	= (float) Math.log(0.5);

	private int					bucketSize		= 0;
	private int					valSize			= 0;
	private int[]				bucket			= null;
	private int[]				nextPosArr		= null;
	private char[]				ch1Arr			= null;
	private char[]				ch2Arr			= null;
	private float[]				interProbArr	= null;
	private float[]				intraProbArr	= null;


	public LnprSyllableBiDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));

			bucketSize = bbr.readInt();
			valSize = bbr.readInt();

			bucket = bbr.readIntA(bucketSize);
			nextPosArr = bbr.readIntA(valSize);

			ch1Arr = bbr.readCharA(valSize);
			ch2Arr = bbr.readCharA(valSize);

			interProbArr = bbr.readFloatA(valSize);
			intraProbArr = bbr.readFloatA(valSize);

			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(char ch1, char ch2, boolean isSpacing)
	{
		int bucketPos = Math.abs(RSHash.hash(ch1, ch2)) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, ch1, ch2) ) return isSpacing ? interProbArr[valPos] : intraProbArr[valPos];
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, ch1, ch2) ) return isSpacing ? interProbArr[valPos] : intraProbArr[valPos];
			}
		}
		return DEFAULT_PROB;
	}


	private boolean matched(int valPos, char ch1, char ch2)
	{
		if( ch1Arr[valPos] == ch1 && ch2Arr[valPos] == ch2 ) return true;
		return false;
	}
}
