package org.snu.ids.ha.dic;


import org.snu.ids.ha.constants.POSTag;
import org.snu.ids.ha.util.RSHash;
import org.snu.ids.ha.util.Timer;


/**
 * <pre>
 * 기호에 대한 구분을 하기 위한 클래스
 * </pre>
 * @author 	therocks
 * @since	2009. 10. 07
 */
public class SymbolDic
{
	private static int		bucketSize		= -1;
	private static int[]	bucket			= null;
	private static int[]	nextPosArr		= null;
	private static int[]	keyHeadPosArr	= null;
	private static char[]	keyArr			= null;
	private static long[]	tagArr			= null;

	static {
		// load from dic root
		//load("D:/Eclipse/workspace/org.snu.ids.ha3/dic");
		load("dic");
	}


	public static void load(String dicRoot)
	{
		String fileName = dicRoot + "/compile_symbol.dic";

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
	 * 등록된 심볼에 대한 태그 부착하여 반환
	 * </pre>
	 * @author	Dongjoo
	 * @since	2011. 8. 1.
	 * @param word
	 * @return
	 */
	public static long getSymbolTag(char[] word)
	{
		int bucketPos = Math.abs(RSHash.hash(word)) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, word) ) return tagArr[valPos];
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, word) ) return tagArr[valPos];
			}
		}
		return POSTag.SW;
	}


	private static boolean matched(int valPos, char[] word)
	{
		int keyHeadPos = keyHeadPosArr[valPos];
		int keyLen = (valPos + 1 < keyHeadPosArr.length ? keyHeadPosArr[valPos + 1] : keyArr.length) - keyHeadPos;
		if( keyLen != word.length ) return false;
		for( int i = 0; i < keyLen; i++ ) {
			if( keyArr[keyHeadPos + i] != word[i] ) return false;
		}
		return true;
	}


	public static void main(String[] args)
	{
		System.out.println(POSTag.getTag(SymbolDic.getSymbolTag("-".toCharArray())));
	}
}