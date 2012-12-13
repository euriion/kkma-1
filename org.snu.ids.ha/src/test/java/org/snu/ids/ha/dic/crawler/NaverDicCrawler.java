package org.snu.ids.ha.dic.crawler;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;


public class NaverDicCrawler
{
	public static void main(String[] args)
	{
		NaverDicCrawler crawler = new NaverDicCrawler();
		//crawler.crawlCat();
		//crawler.readMoreCat();
		//crawler.printCatTree();
		//crawler.parseCatPage();
		//System.out.println(crawler.parseCatPage("file:///D:\\git-local\\kkma\\org.snu.ids.ha\\cat\\51.html", "54", 1));
		crawler.printWord();

	}


	Logger	logger	= Logger.getLogger(NaverDicCrawler.class);


	public void printWord()
	{
		File dir = new File("cat/data/");
		BufferedReader br = null;
		String line = null;
		PrintWriter pw = null;
		try {
			pw = new PrintWriter("word.txt", "utf-8");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for( File file : dir.listFiles() ) {
			String catId = file.getName().replaceAll("[.]txt", "");
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
				while( (line = br.readLine()) != null ) {
					pw.println(line.substring(0, line.indexOf(',')) + "\t\t" + catId);
				}
				br.close();
				pw.flush();
			} catch (Exception e) {
				System.err.println(file);
				System.err.println(line);
				e.printStackTrace();
			}
		}
		pw.close();
	}


	public void printCatTree()
	{
		List<DicCat> catList = getCatList();
		for( DicCat dicCat : catList ) {
			for( int i = 0; i < dicCat.depth; i++ ) {
				System.out.print("\t");
			}
			System.out.println(dicCat.id + ":" + dicCat.name);
		}
	}


	public List<DicCat> getCatList()
	{
		List<DicCat> catList = null;

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("naver_cat_all.txt"), "utf-8"));
			String line = null;
			DicCat cat = null;
			Hashtable<String, DicCat> catMap = new Hashtable<String, DicCat>();
			while( (line = br.readLine()) != null ) {
				String[] temp = line.split(",");
				if( temp[0].equals("null") ) {
					cat = new DicCat(null, temp[1].trim(), temp[2].trim());
				} else {
					cat = new DicCat(temp[0].trim(), temp[1].trim(), temp[2].trim());
				}
				catMap.put(cat.id, cat);
			}

			// set parent
			DicCat parentCat = null;
			for( DicCat dc : catMap.values() ) {
				if( dc.parentId == null ) {
					continue;
				}
				parentCat = catMap.get(dc.parentId);
				parentCat.add(dc);
			}

			// set first depth
			List<DicCat> topCatList = new ArrayList<DicCat>();
			for( DicCat dc : catMap.values() ) {
				if( dc.parentId == null ) {
					topCatList.add(dc);
				}
			}

			// sort top nodes
			Collections.sort(topCatList);

			// print
			catList = new ArrayList<DicCat>();
			for( DicCat dc : topCatList ) {
				setParent(dc, 0, catList);
			}

		} catch (Exception e) {
			logger.error("Error", e);
		}
		return catList;
	}


	public void setParent(DicCat dc, int depth, List<DicCat> catList)
	{
		dc.depth = depth;
		catList.add(dc);
		if( dc.children != null ) {
			// sort top nodes
			Collections.sort(dc.children);
			for( DicCat child : dc.children ) {
				setParent(child, depth + 1, catList);
			}
		}
	}


	public void readMoreCat()
	{
		File dir = new File("cat/");
		PrintWriter pw = null;
		try {
			pw = new PrintWriter("naver_cat_more.txt", "utf-8");
			for( File file : dir.listFiles() ) {
				if( file.isFile() ) {
					String catId = file.getName().replaceAll("[.]html", "");
					readMoreCat("file:///" + file.getAbsolutePath(), catId, true, pw);
				}
			}
			pw.close();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}


	public void readMoreCat(String urlStr, String catId, boolean isLeft, PrintWriter pw)
	{
		try {
			Source source = new Source(new URL(urlStr));
			List<net.htmlparser.jericho.Element> elist = source.getAllElements("div");
			for( net.htmlparser.jericho.Element elm : elist ) {
				String className = elm.getAttributeValue("class");
				if( className != null ) {
					if( isLeft && className.equals("cat_lft") || !isLeft && className.equals("cat_rgt") ) {
						for( net.htmlparser.jericho.Element e1 : elm.getAllElements("a") ) {
							String temp = e1.getAttributeValue("href");
							temp = temp.substring(temp.indexOf('=') + 1);
							pw.println(catId + ", " + temp + ", " + e1.getAttributeValue("title"));
							if( isLeft ) readMoreCat("http://terms.naver.com/list.nhn?cid=" + catId + "&categoryId=" + temp, temp, false, pw);
						}
					}
				}
			}
			pw.flush();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}


	public List<String> getTempCatIdList()
	{
		List<String> catIdList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("cat/data/dir.txt"));
			String line = null;
			while( (line = br.readLine()) != null ) {
				catIdList.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return catIdList;
	}


	public void parseCatPage()
	{
		List<DicCat> catList = getCatList();
		List<String> catIdList = new ArrayList<String>();
		for( DicCat dicCat : catList ) {
			catIdList.add(dicCat.id);
		}

		catIdList = getTempCatIdList();

		for( String catId : catIdList ) {
			System.out.println(catId);
			PrintWriter pw = null;
			try {
				boolean noResult = false;
				pw = new PrintWriter("cat/data/" + catId + ".txt", "utf-8");
				ParseCatResult ret = new ParseCatResult(catId, 1);
				for( int i = 1; i <= ret.maxPage; i++ ) {
					System.out.println("\t" + i);
					String urlStr = BASE_URL + "/list.nhn?categoryId=" + catId + "&page=" + i;
					try {
						ret = parseCatPage(urlStr, ret.catId, pw);
						if( ret.maxPage == -1 ) noResult = true;
					} catch (Exception e) {
						logger.error(urlStr, e);
					}
				}
				pw.close();
				if( noResult ) {
					new File("cat/data/" + catId + ".txt").delete();
				}
			} catch (FileNotFoundException e) {
				logger.error("", e);
			} catch (UnsupportedEncodingException e) {
				logger.error("", e);
			}
		}
	}


	public static final String	BASE_URL	= "http://terms.naver.com";


	public ParseCatResult parseCatPage(String urlStr, String catId, PrintWriter pw)
		throws Exception
	{
		int maxPage = 1;
		try {
			Source source = new Source(new URL(urlStr));
			List<net.htmlparser.jericho.Element> elist = source.getAllElements("div");
			for( net.htmlparser.jericho.Element elm : elist ) {
				String className = elm.getAttributeValue("class");
				if( className != null ) {
					if( className.equals("lst_nav") ) {
						for( net.htmlparser.jericho.Element e1 : elm.getAllElements("a") ) {
							String temp = e1.getAttributeValue("href");
							temp = temp.substring(temp.indexOf('=') + 1, temp.indexOf('&'));
							if( !catId.equals(temp) ) return new ParseCatResult(catId, -1);
							break;
						}
					} else if( className.equals("lst") ) {
						for( net.htmlparser.jericho.Element e1 : elm.getAllElements("a") ) {
							pw.println(e1.getAttributeValue("title") + ", " + e1.getAttributeValue("href"));
						}
					} else if( className.equals("paginate") ) {
						for( net.htmlparser.jericho.Element e1 : elm.getAllElements("a") ) {
							String temp = e1.getAttributeValue("href");
							if( temp.indexOf("categoryId=" + catId + "&") < 0 ) {
								return new ParseCatResult(catId, -1);
							}
							temp = temp.substring(temp.indexOf("page=") + 5);
							maxPage = Integer.parseInt(temp);
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return new ParseCatResult(catId, maxPage);
	}


	public void crawlCat()
	{
		HashSet<String> catIdSet = new HashSet<String>();
		List<String> candList = new ArrayList<String>();
		candList.add("392");

		Set<DicCat> catSet = new HashSet<DicCat>();

		PrintWriter pw = null;
		try {
			pw = new PrintWriter("naver_cat.txt", "utf-8");

			while( candList.size() > 0 ) {
				String catId = candList.remove(0);
				catIdSet.add(catId);
				List<DicCat> catList = crawlCat(catId);
				if( catList != null ) {
					for( DicCat cat : catList ) {
						if( !catSet.contains(cat) ) {
							catSet.add(cat);
							pw.println(cat);
						}
						if( !catIdSet.contains(cat.id) ) {
							candList.add(cat.id);
							catIdSet.add(cat.id);
						}
					}
				}
				System.out.println(catId);
			}

			pw.close();
		} catch (Exception e) {
			logger.error("Error", e);
		}
	}


	public List<DicCat> crawlCat(String catId)
	{
		List<DicCat> ret = null;

		URL url = null;
		URLConnection urlCon = null;

		BufferedReader br = null;
		PrintWriter pw = null;
		try {
			String urlStr = BASE_URL + "/list.nhn?cid=" + catId + "&categoryId=" + catId;
			url = new URL(urlStr);
			urlCon = url.openConnection();
			br = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), "utf-8"));
			pw = new PrintWriter("cat/" + catId + ".html", "utf-8");

			String line = null;
			while( (line = br.readLine()) != null ) {
				pw.println(line);
				if( line.trim().startsWith("<li class=\"category") ) {
					line = line.trim().replaceAll("</*a[^>]*>", "");
					line = line.replaceAll("<img[^>]*>", "");
					line = line.replaceAll("<button class=\"btn_tree\" type=\"button\"><span>카테고리 여닫기</span></button>", "");
					line = line.trim().replaceAll("</*span>", "");
					line = line.trim().replaceAll(" class[=][\"][^\"]*[\"]", "");
					ret = parseDicCatXml("<category>" + line + "</category>");
				}
			}
			br.close();
			pw.close();
		} catch (Exception e) {
			logger.error("Error", e);
		}
		return ret;
	}


	public List<DicCat> parseDicCatXml(String xmlStr)
	{
		List<DicCat> ret = null;

		SAXBuilder sax = new SAXBuilder();
		try {
			Document doc = sax.build(new StringReader(xmlStr));
			ret = getChildren(doc.getRootElement(), null);
		} catch (Exception e) {
			logger.error("Error", e);
		}

		return ret;
	}


	public List<DicCat> getChildren(Element parent, String parentId)
	{
		List<DicCat> ret = new ArrayList<DicCat>();

		for( Element e : parent.getChildren() ) {
			String categoryId = e.getAttributeValue("categoryId");
			if( categoryId != null ) {
				String categoryName = e.getTextTrim();
				DicCat cat = new DicCat(parentId, categoryId, categoryName);
				ret.add(cat);
			}

			if( e.getChildren() != null ) {
				ret.addAll(getChildren(e, categoryId == null ? parentId : categoryId));
			}

		}

		return ret;
	}

}

class DicCat
	implements Comparable<DicCat>
{
	String	parentId	= null;
	String	id			= null;
	String	name		= null;


	public DicCat(String parentId, String id, String name)
	{
		super();
		this.parentId = parentId;
		this.id = id;
		this.name = name;
	}


	@Override
	public int hashCode()
	{
		return (parentId + ":" + id + ":" + name).hashCode();
	}


	@Override
	public boolean equals(Object obj)
	{
		if( this == obj ) return true;
		if( obj == null ) return false;
		if( !(obj instanceof DicCat) ) return false;
		DicCat other = (DicCat) obj;
		if( id == null ) {
			if( other.id != null ) return false;
		} else if( !id.equals(other.id) ) return false;
		if( name == null ) {
			if( other.name != null ) return false;
		} else if( !name.equals(other.name) ) return false;
		if( parentId == null ) {
			if( other.parentId != null ) return false;
		} else if( !parentId.equals(other.parentId) ) return false;
		return true;
	}


	public String toString()
	{
		return parentId + ", " + id + ", " + name;
	}


	List<DicCat>	children	= null;


	public void add(DicCat child)
	{
		if( children == null ) children = new ArrayList<DicCat>();
		children.add(child);
	}


	@Override
	public int compareTo(DicCat o)
	{
		return Integer.parseInt(this.id) - Integer.parseInt(o.id);
	}


	int	depth	= 0;
}

class ParseCatResult
{
	String	catId	= null;
	int		maxPage	= -1;


	public ParseCatResult(String catId, int maxPage)
	{
		super();
		this.catId = catId;
		this.maxPage = maxPage;
	}


	@Override
	public String toString()
	{
		return "ParseCatResult [catId=" + catId + ", maxPage=" + maxPage + "]";
	}

}
