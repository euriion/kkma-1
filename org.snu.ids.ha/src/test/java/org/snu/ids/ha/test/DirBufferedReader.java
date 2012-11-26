/*
 * Copyright (C) 2011 Web Squared Inc. http://websqrd.com
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.snu.ids.ha.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class DirBufferedReader
{
	private BufferedReader	reader;

	private File[]			fileList;
	private int				fileCount;
	private int				fileUsed;
	private String			encoding;


	public DirBufferedReader(File[] fl, String encoding)
		throws IOException
	{
		this.encoding = encoding;

		fileList = new File[fl.length];

		for( int i = 0; i < fl.length; i++ ) {
			if( fl[i].isFile() ) {
				fileList[fileCount] = fl[i];
				fileCount++;
			}
		}

		if( fileList.length > 0 ) {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[0]), encoding));
		}

	}


	public DirBufferedReader(File f, String encoding)
		throws IOException
	{
		this.encoding = encoding;
		if( f.isDirectory() ) {
			File[] fl = f.listFiles();

			fileList = new File[fl.length];

			for( int i = 0; i < fl.length; i++ ) {
				if( fl[i].isFile() ) {
					fileList[fileCount] = fl[i];
					fileCount++;
				}
			}

		} else {
			fileCount = 1;
			fileList = new File[fileCount];
			fileList[0] = f;
		}

		if( fileCount == 0 ) {
			throw new IOException("There's no source file in directory " + f.getAbsolutePath());
		}

		if( fileList.length > 0 ) {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[0]), encoding));
		}

	}


	public String readLine()
		throws IOException
	{
		String line = reader.readLine();

		if( line == null ) {
			try {
				reader.close();
				fileUsed++;
			} catch (IOException e) {
				//ignore
			}

			while( fileUsed < fileCount && !fileList[fileUsed].exists() ) {
				fileUsed++;
			}

			if( fileUsed == fileCount ) {
				return null;
			}

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileList[fileUsed]), encoding));
			line = reader.readLine();
		}

		return line;
	}


	public void close()
		throws IOException
	{
		reader.close();
	}
}
