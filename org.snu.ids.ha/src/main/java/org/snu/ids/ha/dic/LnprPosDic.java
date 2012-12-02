package org.snu.ids.ha.dic;


import org.snu.ids.ha.util.Timer;


public class LnprPosDic
{
	private float[]	bucket	= null;


	public LnprPosDic(String fileName)
	{
		ByteBufferedReader bbr = null;

		Timer timer = new Timer();
		timer.start();

		try {
			// bbr = new ByteBufferedReader(new FileInputStream(fileName));
			bbr = new ByteBufferedReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
			bucket = bbr.readFloatA(64);
			bbr.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			//timer.printMsg(fileName);
		}
	}


	public float get(long tag)
	{
		return bucket[Long.numberOfLeadingZeros(tag)];
	}
}
