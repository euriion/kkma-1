package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.Timer;


public class LnprSyllableUniNounDic
{
	private static final float	DEFAULT_PROB	= -3000f;

	private int					min				= 0;
	private int					bucketSize		= 0;
	private float[]				bucket			= null;


	public LnprSyllableUniNounDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(ClassLoader.getSystemResourceAsStream(fileName));

			min = bbr.readInt();
			bucketSize = bbr.readInt();
			bucket = bbr.readFloatA(bucketSize);

			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(char ch)
	{
		int pos = ch - min;
		if( pos < 0 || pos >= bucketSize ) return DEFAULT_PROB;
		return bucket[pos];
	}
}
