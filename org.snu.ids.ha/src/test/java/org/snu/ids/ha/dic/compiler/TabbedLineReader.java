package org.snu.ids.ha.dic.compiler;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.snu.ids.ha.util.Util;



public class TabbedLineReader
{
	private BufferedReader	br		= null;
	String					line	= null;


	public TabbedLineReader(String fileName)
		throws UnsupportedEncodingException, FileNotFoundException
	{

		br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
	}


	public String[] read()
		throws IOException
	{
		while( (line = br.readLine()) != null ) {
			line = line.trim();
			if( !Util.valid(line) || line.startsWith("//") ) continue;
			return line.split("\t");
		}

		return null;
	}


	public void close()
		throws IOException
	{
		if( br != null ) br.close();
	}
}