package org.snu.ids.ha.dic;


import org.snu.ids.ha.core.CharArray;
import org.snu.ids.ha.core.MCandidate;
import org.snu.ids.ha.util.Timer;


public class MorpDic
{
	static MorpDic	dic	= null;


	public static MorpDic getInstance()
	{
		// TODO
		if( dic == null ) {
			// load from dic root
			//load("D:/Eclipse/workspace/org.snu.ids.ha3/dic");
			load("dic");
		}
		return dic;
	}


	public static void load(String dicRoot)
	{
		long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long prevUseMem = totalMem - freeMem;
		Timer timer = new Timer();
		timer.start();
		dic = new MorpDic(dicRoot + "/compile.dic");
		ProbDicSet.load(dicRoot);
		timer.stop();
		timer.printMsg("Dic is loaded.");
		System.gc();
		freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long curUseMem = totalMem - freeMem;
		System.out.println((curUseMem - prevUseMem) + "MB used for dictionary");
	}


	// init memory for hashing
	private int				bucketSize		= 0;
	private int[]			bucket			= null;
	private int[]			nextPosArr		= null;
	private int[]			keyHeadPosArr	= null;
	private char[]			keyArr			= null;
	private MCandidate[][]	val				= null;


	private MorpDic(String fileName)
	{
		Timer timer = new Timer();
		timer.start();

		ByteBufferedReader bbr = null;
		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(ClassLoader.getSystemResourceAsStream(fileName));

			bucketSize = bbr.readInt();
			int valSize = bbr.readInt();
			int keyArrSize = bbr.readInt();

			bucket = bbr.readIntA(bucketSize);
			nextPosArr = bbr.readIntA(valSize);
			keyHeadPosArr = bbr.readIntA(valSize);
			keyArr = bbr.readCharA(keyArrSize);
			val = new MCandidate[valSize][];

			for( int i = 0; i < valSize; i++ ) {

				float lnprOfSpacing = bbr.readFloat();

				int mcSize = bbr.readByte();

				MCandidate[] mcl = val[i] = new MCandidate[mcSize];
				for( int j = 0; j < mcSize; j++ ) {
					MCandidate mc = mcl[j] = new MCandidate();
					mc.setLnprOfSpacing(lnprOfSpacing);

					int mlSize = bbr.readByte();
					char[][] wordArr = new char[mlSize][];
					for( int k = 0; k < mlSize; k++ ) {
						int len = bbr.readByte();
						wordArr[k] = bbr.readCharA(len);
					}
					long[] infoEncArr = bbr.readLongA(mlSize);

					mc.setWordArr(wordArr);
					mc.setInfoEncArr(infoEncArr);
					mc.setLastMorpIdx(wordArr.length - 1);

					int cnSize = bbr.readByte();
					if( cnSize > 0 ) {
						char[][] compNounArr = new char[cnSize][];
						for( int k = 0; k < cnSize; k++ ) {
							int len = bbr.readByte();
							compNounArr[k] = bbr.readCharA(len);
						}
						mc.setCompNounArr(compNounArr);
					}

					mc.addApndblTag(bbr.readLong());
					mc.addHavingCond(bbr.readInt());
					mc.addBackwardCond(bbr.readInt());
					mc.addChkCond(bbr.readInt());
					mc.addExclusionCond(bbr.readInt());
					mc.setLnprOfTagging(bbr.readFloat());
				}

			}

			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}

	}


	public MCandidate[] get(CharArray exp)
	{
		int bucketPos = Math.abs(exp.hashCode()) % bucketSize;
		int valPos = bucket[bucketPos];
		if( valPos > -1 ) {
			if( matched(valPos, exp) ) return makeClone(val[valPos]);
			// collision bucket search
			while( nextPosArr[valPos] != -1 ) {
				valPos = nextPosArr[valPos];
				if( matched(valPos, exp) ) return makeClone(val[valPos]);
			}
		}

		return null;
	}


	private static MCandidate[] makeClone(MCandidate[] cands)
	{
		MCandidate[] clone = new MCandidate[cands.length];
		for( int i = 0, size = cands.length; i < size; i++ ) {
			clone[i] = cands[i].clone();
		}
		return clone;
	}


	private boolean matched(int valPos, CharArray exp)
	{
		int keyHeadPos = keyHeadPosArr[valPos];
		int keyLen = (valPos + 1 < keyHeadPosArr.length ? keyHeadPosArr[valPos + 1] : keyArr.length) - keyHeadPos;
		if( keyLen != exp.length ) return false;
		for( int i = 0; i < keyLen; i++ ) {
			if( keyArr[keyHeadPos + i] != exp.array[exp.start + i] ) return false;
		}
		return true;
	}


	public static void main(String[] args)
	{
		long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		long useMem = totalMem - freeMem;
		System.out.println(String.format("%10d%10d%10d", freeMem, totalMem, useMem));
		MorpDic dic = MorpDic.getInstance();
		System.gc();
		freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
		totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
		useMem = totalMem - freeMem;
		System.out.println(String.format("%10d%10d%10d", freeMem, totalMem, useMem));
		MCandidate[] cands = dic.get(new CharArray("로와"));
		for( MCandidate mc : cands ) {
			System.out.println(mc.toString());
		}
	}
}
