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


public class ProbDicCompiler
{
	public static void main(String[] args)
	{
		// 음절 확률 사전
		compileLnprSyllableBi(DicCompiler.DIC_ROOT + "/prob/lnpr_syllable_bi.txt", "src/main/resources/dic/compile_lnpr_syllable_bi.dic");
		ProbDicCompiler.compileLnprSyllableUniNoun(DicCompiler.DIC_ROOT + "/prob/lnpr_syllable_uni_noun.txt", "src/main/resources/dic/compile_lnpr_syllable_uni_noun.dic");

		// 확률 사전 compile
		compileLnprPos(DicCompiler.DIC_ROOT + "/prob/lnpr_pos.txt", "src/main/resources/dic/compile_lnpr_pos.dic");
		compilePosGPos(DicCompiler.DIC_ROOT + "/prob/lnpr_pos_g_pos_intra.txt", "src/main/resources/dic/compile_lnpr_pos_g_pos_intra.dic");
		compilePosGPos(DicCompiler.DIC_ROOT + "/prob/lnpr_pos_g_pos_inter.txt", "src/main/resources/dic/compile_lnpr_pos_g_pos_inter.dic");
		compileLnprMorp(DicCompiler.DIC_ROOT + "/prob/lnpr_morp.txt", "src/main/resources/dic/compile_lnpr_morp.dic");
		compileLnprMorp(DicCompiler.DIC_ROOT + "/prob/lnpr_pos_g_exp.txt", "src/main/resources/dic/compile_lnpr_pos_g_exp.dic");
		compileLnprMorpsGExp(DicCompiler.DIC_ROOT + "/prob/lnpr_morps_g_exp.txt", "src/main/resources/dic/compile_lnpr_morps_g_exp.dic");
		compileLnprPosGMorp(DicCompiler.DIC_ROOT + "/prob/lnpr_pos_g_morp_intra.txt", "src/main/resources/dic/compile_lnpr_pos_g_morp_intra.dic");
		compileLnprPosGMorp(DicCompiler.DIC_ROOT + "/prob/lnpr_pos_g_morp_inter.txt", "src/main/resources/dic/compile_lnpr_pos_g_morp_inter.dic");
	}


	public static void compileLnprSyllableUniNoun(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);

			final int min = 44032, max = 55197;
			final int bucketSiz = max - min + 1;
			float[] bucket = new float[bucketSiz];
			Arrays.fill(bucket, 0.5f);

			String[] arr = null;
			while( (arr = dicReader.read()) != null ) {
				char ch = arr[0].charAt(0);
				int pos = ch - min;
				bucket[pos] = Float.parseFloat(arr[1]);
			}

			// init writer
			fos = new FileOutputStream(targetFileName);

			// write prob bucket
			byte[] temp = null;

			// write size information
			temp = Convert.toByta(min);
			fos.write(temp);
			temp = Convert.toByta(bucketSiz);
			fos.write(temp);
			temp = Convert.toByta(bucket);
			fos.write(temp);

			dicReader.close();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compileLnprSyllableBi(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;
			ArrayList<LnprSyllableBi> probObjList = new ArrayList<LnprSyllableBi>();

			final int bucketSize = 100000;

			while( (arr = dicReader.read()) != null ) {
				LnprSyllableBi probObj = new LnprSyllableBi(arr);
				probObjList.add(probObj);
			}
			Collections.sort(probObjList, new Comparator<LnprSyllableBi>()
			{
				public int compare(LnprSyllableBi arg0, LnprSyllableBi arg1)
				{
					return arg0.getBucketPos(bucketSize) - arg1.getBucketPos(bucketSize);
				}
			});

			int valSize = probObjList.size();

			// construct hash
			int[] bucket = new int[bucketSize];
			int[] nextPosArr = new int[valSize];
			char[] ch1Arr = new char[valSize];
			char[] ch2Arr = new char[valSize];
			float[] interProbArr = new float[valSize];
			float[] intraProbArr = new float[valSize];

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);

			int valPos = 0, bucketPos = 0, curPos = 0;
			for( LnprSyllableBi probObj : probObjList ) {
				bucketPos = probObj.getBucketPos(bucketSize);
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
				ch1Arr[curPos] = probObj.ch1;
				ch2Arr[curPos] = probObj.ch2;
				interProbArr[curPos] = probObj.interProb;
				intraProbArr[curPos] = probObj.intraProb;

				// increase memory position
				curPos++;
			}

			byte[] temp = null;

			// write size information
			temp = Convert.toByta(bucketSize);
			fos.write(temp);
			temp = Convert.toByta(valSize);
			fos.write(temp);
			// write hash
			temp = Convert.toByta(bucket);
			fos.write(temp);
			temp = Convert.toByta(nextPosArr);
			fos.write(temp);
			temp = Convert.toByta(ch1Arr);
			fos.write(temp);
			temp = Convert.toByta(ch2Arr);
			fos.write(temp);
			temp = Convert.toByta(interProbArr);
			fos.write(temp);
			temp = Convert.toByta(intraProbArr);
			fos.write(temp);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compilePosGPos(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;

			float[][] bucket = new float[64][64];
			for( int i = 0; i < 64; i++ )
				Arrays.fill(bucket[i], -1000f);

			while( (arr = dicReader.read()) != null ) {
				long prevTag = POSTag.getTagNum(arr[0]);
				long givenTag = POSTag.getTagNum(arr[1]);
				float prob = Float.parseFloat(arr[2]);
				int bucketPos1 = Long.numberOfLeadingZeros(prevTag);
				int bucketPos2 = Long.numberOfLeadingZeros(givenTag);
				if( bucket[bucketPos1][bucketPos2] != -1000f ) {
					System.out.println("Collision\t" + String.format("%4s %4s %6d %6d %64s", arr[0], arr[1], bucketPos1, bucketPos2, Long.toBinaryString(prevTag)) + "\t" + prob);
				} else {
					bucket[bucketPos1][bucketPos2] = prob;
				}
			}
			byte[] temp = null;
			for( int i = 0; i < 64; i++ ) {
				temp = Convert.toByta(bucket[i]);
				fos.write(temp);
			}
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compileLnprPosGMorp(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;
			ArrayList<LnprPosGMorp> probObjList = new ArrayList<LnprPosGMorp>();

			final int bucketSize = 100000;
			int valSize = 0;
			int nextWordArrSize = 0;
			while( (arr = dicReader.read()) != null ) {
				LnprPosGMorp probObj = new LnprPosGMorp(arr);
				probObjList.add(probObj);
				nextWordArrSize += probObj.nextWord.length;
				valSize++;
			}
			Collections.sort(probObjList, new Comparator<LnprPosGMorp>()
			{
				public int compare(LnprPosGMorp arg0, LnprPosGMorp arg1)
				{
					return arg0.getBucketPos(bucketSize) - arg1.getBucketPos(bucketSize);
				}
			});

			// construct hash
			int[] bucket = new int[bucketSize];
			int[] nextPosArr = new int[valSize];
			int[] nextWordHeadPosArr = new int[valSize];
			char[] nextWordArr = new char[nextWordArrSize];
			long[] prevTagArr = new long[valSize];
			long[] nextTagArr = new long[valSize];
			float[] valArr = new float[valSize];

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);

			int valPos = 0, bucketPos = 0, curPos = 0, curNextWordHeadPos = 0;
			for( LnprPosGMorp probObj : probObjList ) {
				bucketPos = probObj.getBucketPos(bucketSize);
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
				nextWordHeadPosArr[curPos] = curNextWordHeadPos;
				System.arraycopy(probObj.nextWord, 0, nextWordArr, curNextWordHeadPos, probObj.nextWord.length);
				prevTagArr[curPos] = probObj.prevTag;
				nextTagArr[curPos] = probObj.nextTag;
				valArr[curPos] = probObj.prob;

				// increase memory position
				curNextWordHeadPos += probObj.nextWord.length;
				curPos++;
			}

			byte[] temp = null;

			// write size information
			temp = Convert.toByta(bucketSize);
			fos.write(temp);
			temp = Convert.toByta(valSize);
			fos.write(temp);
			temp = Convert.toByta(nextWordArrSize);
			fos.write(temp);

			// write hash
			temp = Convert.toByta(bucket);
			fos.write(temp);
			temp = Convert.toByta(nextPosArr);
			fos.write(temp);
			temp = Convert.toByta(nextWordHeadPosArr);
			fos.write(temp);
			temp = Convert.toByta(nextWordArr);
			fos.write(temp);
			temp = Convert.toByta(prevTagArr);
			fos.write(temp);
			temp = Convert.toByta(nextTagArr);
			fos.write(temp);
			temp = Convert.toByta(valArr);
			fos.write(temp);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compileLnprPos(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;

			float[] bucket = new float[64];
			Arrays.fill(bucket, -1000f);
			while( (arr = dicReader.read()) != null ) {
				long tag = POSTag.getTagNum(arr[0]);
				float prob = Float.parseFloat(arr[1]);
				int bucketPos = Long.numberOfLeadingZeros(tag);
				if( bucket[bucketPos] != -1000f ) {
					System.out.println("Collision\t" + String.format("%4s %6d %64s", arr[0], bucketPos, Long.toBinaryString(tag)) + "\t" + prob);
				} else {
					bucket[bucketPos] = prob;
				}
			}
			byte[] temp = Convert.toByta(bucket);
			fos.write(temp);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compileLnprMorpsGExp(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;
			ArrayList<LnprMorpsGExp> probObjList = new ArrayList<LnprMorpsGExp>();

			final int bucketSize = 20000;
			int valSize = 0;
			int prevWordArrSize = 0;
			int nextWordArrSize = 0;
			while( (arr = dicReader.read()) != null ) {
				LnprMorpsGExp probObj = new LnprMorpsGExp(arr);
				probObjList.add(probObj);
				prevWordArrSize += probObj.prevWord.length;
				nextWordArrSize += probObj.nextWord.length;
				valSize++;
			}
			Collections.sort(probObjList, new Comparator<LnprMorpsGExp>()
			{
				public int compare(LnprMorpsGExp arg0, LnprMorpsGExp arg1)
				{
					return arg0.getBucketPos(bucketSize) - arg1.getBucketPos(bucketSize);
				}
			});

			// construct hash
			int[] bucket = new int[bucketSize];
			int[] nextPosArr = new int[valSize];
			int[] prevWordHeadPosArr = new int[valSize];
			int[] nextWordHeadPosArr = new int[valSize];
			char[] prevWordArr = new char[prevWordArrSize];
			char[] nextWordArr = new char[nextWordArrSize];
			long[] prevTagArr = new long[valSize];
			long[] nextTagArr = new long[valSize];
			float[] valArr = new float[valSize];

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);

			int valPos = 0, bucketPos = 0, curPos = 0, curPrevWordHeadPos = 0, curNextWordHeadPos = 0;
			for( LnprMorpsGExp probObj : probObjList ) {
				bucketPos = probObj.getBucketPos(bucketSize);
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
				prevWordHeadPosArr[curPos] = curPrevWordHeadPos;
				nextWordHeadPosArr[curPos] = curNextWordHeadPos;
				System.arraycopy(probObj.prevWord, 0, prevWordArr, curPrevWordHeadPos, probObj.prevWord.length);
				System.arraycopy(probObj.nextWord, 0, nextWordArr, curNextWordHeadPos, probObj.nextWord.length);
				prevTagArr[curPos] = probObj.prevTag;
				nextTagArr[curPos] = probObj.nextTag;
				valArr[curPos] = probObj.prob;

				// increase memory position
				curPrevWordHeadPos += probObj.prevWord.length;
				curNextWordHeadPos += probObj.nextWord.length;
				curPos++;
			}

			byte[] temp = null;

			// write size information
			temp = Convert.toByta(bucketSize);
			fos.write(temp);
			temp = Convert.toByta(valSize);
			fos.write(temp);
			temp = Convert.toByta(prevWordArrSize);
			fos.write(temp);
			temp = Convert.toByta(nextWordArrSize);
			fos.write(temp);

			// write hash
			temp = Convert.toByta(bucket);
			fos.write(temp);
			temp = Convert.toByta(nextPosArr);
			fos.write(temp);
			temp = Convert.toByta(prevWordHeadPosArr);
			fos.write(temp);
			temp = Convert.toByta(nextWordHeadPosArr);
			fos.write(temp);
			temp = Convert.toByta(prevWordArr);
			fos.write(temp);
			temp = Convert.toByta(nextWordArr);
			fos.write(temp);
			temp = Convert.toByta(prevTagArr);
			fos.write(temp);
			temp = Convert.toByta(nextTagArr);
			fos.write(temp);
			temp = Convert.toByta(valArr);
			fos.write(temp);

			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			timer.stop();
			timer.printMsg(targetFileName);
		}
	}


	public static void compileLnprMorp(String srcFileName, String targetFileName)
	{
		Timer timer = new Timer();
		timer.start();
		TabbedLineReader dicReader = null;
		FileOutputStream fos = null;
		try {
			dicReader = new TabbedLineReader(srcFileName);
			fos = new FileOutputStream(targetFileName);
			String[] arr = null;
			ArrayList<LnprMorp> probObjList = new ArrayList<LnprMorp>();

			int valSize = 0;
			int keyArrSize = 0;
			final int bucketSize = 10000;

			while( (arr = dicReader.read()) != null ) {
				LnprMorp probObj = new LnprMorp(arr);
				probObjList.add(probObj);
				keyArrSize += probObj.word.length;
				valSize++;
			}
			Collections.sort(probObjList, new Comparator<LnprMorp>()
			{
				public int compare(LnprMorp arg0, LnprMorp arg1)
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
			float[] valArr = new float[valSize];

			Arrays.fill(bucket, -1);
			Arrays.fill(nextPosArr, -1);

			int valPos = 0, bucketPos = 0, curPos = 0, curKeyHeadPos = 0;
			for( LnprMorp probObj : probObjList ) {
				bucketPos = probObj.getBucketPos(bucketSize);
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
				System.arraycopy(probObj.word, 0, keyArr, curKeyHeadPos, probObj.word.length);
				tagArr[curPos] = probObj.tag;
				valArr[curPos] = probObj.prob;

				// increase memory position
				curKeyHeadPos += probObj.word.length;
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
			temp = Convert.toByta(valArr);
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

class LnprMorp
{
	char[]	word	= null;
	long	tag		= 0l;
	float	prob	= 0;
	int		hash	= 0;


	LnprMorp(String[] arr)
	{
		this.word = arr[0].toCharArray();
		this.tag = POSTag.getTagNum(arr[1]);
		this.prob = Float.parseFloat(arr[2]);
		this.hash = RSHash.hash(word, tag);
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

class LnprMorpsGExp
{
	char[]	prevWord	= null;
	long	prevTag		= 0l;
	char[]	nextWord	= null;
	long	nextTag		= 0l;
	float	prob		= 0;

	int		hash		= 0;


	LnprMorpsGExp(String[] arr)
	{
		String[] temp = arr[0].split("[+/]");
		this.prevWord = temp[0].toCharArray();
		this.prevTag = POSTag.getTagNum(temp[1]);
		this.nextWord = temp[2].toCharArray();
		this.nextTag = POSTag.getTagNum(temp[3]);

		this.prob = Float.parseFloat(arr[1]);
		this.hash = RSHash.hash(prevWord, prevTag, nextWord, nextTag);
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

class LnprPosGMorp
{
	long	prevTag		= 0l;
	char[]	nextWord	= null;
	long	nextTag		= 0l;
	float	prob		= 0;

	int		hash		= 0;


	LnprPosGMorp(String[] arr)
	{
		this.prevTag = POSTag.getTagNum(arr[0]);
		this.nextWord = arr[1].toCharArray();
		this.nextTag = POSTag.getTagNum(arr[2]);
		this.prob = Float.parseFloat(arr[3]);
		this.hash = RSHash.hash(prevTag, nextWord, nextTag);
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

class LnprSyllableBi
{
	char	ch1			= 0;
	char	ch2			= 0;
	float	interProb	= 0;
	float	intraProb	= 0;

	int		hash		= 0;


	LnprSyllableBi(String[] arr)
	{
		this.ch1 = arr[0].charAt(0);
		this.ch2 = arr[1].charAt(0);
		this.interProb = Float.parseFloat(arr[2]);
		this.intraProb = Float.parseFloat(arr[3]);
		this.hash = RSHash.hash(ch1, ch2);
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
