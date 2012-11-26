package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.Timer;


public class LnprPosGPosDic
{
	private float[][]	bucket	= new float[64][];


	public LnprPosGPosDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			//bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(ClassLoader.getSystemResourceAsStream(fileName));
			for( int i = 0; i < 64; i++ ) {
				bucket[i] = bbr.readFloatA(64);
			}
			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(long prevTag, long givenTag)
	{
		return bucket[Long.numberOfLeadingZeros(prevTag)][Long.numberOfLeadingZeros(givenTag)];
	}
}
