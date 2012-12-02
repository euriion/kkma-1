package org.snu.ids.ha.dic.crawler;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class NaverDicCrawler
{
	public static void main(String[] args)
	{
		String urlStr = "http://terms.naver.com/list.nhn?cid=927&categoryId=1972";

		URL url = null;
		URLConnection urlCon = null;

		BufferedReader br = null;

		StringBuffer sb = new StringBuffer();
		try {
			url = new URL(urlStr);
			urlCon = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), "utf-8"));

			String line = null;

			while( (line = br.readLine()) != null ) {
				if( line.trim().startsWith("<li class=\"category \"") ) {
					line = line.trim().replaceAll("</*a[^>]*>", "");
					line = line.replaceAll("<img[^>]*>", "");
					line = line.replaceAll("<button class=\"btn_tree\" type=\"button\"><span>카테고리 여닫기</span></button>", "");
					line = line.trim().replaceAll("</*span>", "");
					System.out.println("<category>" + line + "</category>");
				}
				sb.append(line + "\n");
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
