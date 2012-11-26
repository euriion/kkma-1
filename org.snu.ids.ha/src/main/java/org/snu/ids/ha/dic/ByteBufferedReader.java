package org.snu.ids.ha.dic;


import java.io.IOException;
import java.io.InputStream;


public class ByteBufferedReader
{
	InputStream	is			= null;
	int			bufferSize	= 0;
	byte[]		buffer		= null;
	int			bufferPos	= 0;
	int			limit		= 0;


	public ByteBufferedReader(InputStream is)
		throws IOException
	{
		this(is, 1024 * 8);
	}


	public ByteBufferedReader(InputStream is, int bufferSize)
		throws IOException
	{
		this.is = is;
		this.bufferSize = bufferSize;
		buffer = new byte[bufferSize];
		fillBuffer();
	}


	private int fillBuffer()
		throws IOException
	{
		limit = is.read(buffer);
		bufferPos = 0;
		return limit;
	}


	public void close()
		throws IOException
	{
		is.close();
		buffer = null;
	}


	public byte readByte()
		throws IOException
	{
		if( bufferPos >= limit ) fillBuffer();
		byte b = buffer[bufferPos];
		bufferPos++;
		return b;
	}


	public short readShort()
		throws IOException
	{
		return (short) ((0xff & readByte()) << 8 | (0xff & readByte()) << 0);
	}


	public char readChar()
		throws IOException
	{
		return (char) ((0xff & readByte()) << 8 | (0xff & readByte()) << 0);
	}


	public int readInt()
		throws IOException
	{
		int i = 0;
		if( limit - bufferPos < 4 ) {
			i = (0xff & readByte()) << 24 | (0xff & readByte()) << 16 | (0xff & readByte()) << 8 | (0xff & readByte()) << 0;
		} else {
			i = (0xff & buffer[bufferPos]) << 24 | (0xff & buffer[bufferPos + 1]) << 16 | (0xff & buffer[bufferPos + 2]) << 8 | (0xff & buffer[bufferPos + 3]) << 0;
			bufferPos += 4;
		}
		return i;
	}


	public float readFloat()
		throws IOException
	{
		return Float.intBitsToFloat(readInt());
	}


	public long readLong()
		throws IOException
	{
		long l = 0;
		if( limit - bufferPos < 8 ) {
			l = (long) (0xff & readByte()) << 56 | (long) (0xff & readByte()) << 48 | (long) (0xff & readByte()) << 40 | (long) (0xff & readByte()) << 32 | (long) (0xff & readByte()) << 24 | (long) (0xff & readByte()) << 16 | (long) (0xff & readByte()) << 8 | (long) (0xff & readByte()) << 0;
		} else {
			l = (long) (0xff & buffer[bufferPos]) << 56 | (long) (0xff & buffer[bufferPos + 1]) << 48 | (long) (0xff & buffer[bufferPos + 2]) << 40 | (long) (0xff & buffer[bufferPos + 3]) << 32 | (long) (0xff & buffer[bufferPos + 4]) << 24 | (long) (0xff & buffer[bufferPos + 5]) << 16 | (long) (0xff & buffer[bufferPos + 6]) << 8 | (long) (0xff & buffer[bufferPos + 7]) << 0;
			bufferPos += 8;
		}
		return l;
	}


	public double readDouble()
		throws IOException
	{
		return Double.longBitsToDouble(readLong());
	}


	public byte[] readByteA(int len)
		throws IOException
	{
		byte[] ret = new byte[len];
		is.read(ret);
		return ret;
	}


	public short[] readShortA(int len)
		throws IOException
	{
		short[] ret = new short[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readShort();
		}
		return ret;
	}


	public char[] readCharA(int len)
		throws IOException
	{
		char[] ret = new char[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readChar();
		}
		return ret;
	}


	public int[] readIntA(int len)
		throws IOException
	{
		int[] ret = new int[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readInt();
		}
		return ret;
	}


	public long[] readLongA(int len)
		throws IOException
	{
		long[] ret = new long[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readLong();
		}
		return ret;
	}


	public float[] readFloatA(int len)
		throws IOException
	{
		float[] ret = new float[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readFloat();
		}
		return ret;
	}


	public double[] readDoubleA(int len)
		throws IOException
	{
		double[] ret = new double[len];
		for( int i = 0; i < len; i++ ) {
			ret[i] = readDouble();
		}
		return ret;
	}
}
