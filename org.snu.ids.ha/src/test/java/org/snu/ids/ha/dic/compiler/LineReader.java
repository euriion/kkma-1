package org.snu.ids.ha.dic.compiler;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


public class LineReader
{
	BufferedReader	br	= null;


	public LineReader(String fileName)
		throws UnsupportedEncodingException, FileNotFoundException
	{
		br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
	}


	public String readLine()
		throws IOException
	{
		String line = null;
		while( (line = br.readLine()) != null ) {
			if( line.trim().length() > 0 && !line.startsWith("//") ) return line.trim();
		}
		return null;
	}


	public void cleanup()
		throws IOException
	{
		if( br != null ) br.close();
	}
}