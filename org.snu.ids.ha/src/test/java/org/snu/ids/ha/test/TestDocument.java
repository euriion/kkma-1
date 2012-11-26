package org.snu.ids.ha.test;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.snu.ids.ha.constants.POSTag;


public class TestDocument
	extends ArrayList<ANSSentence>
{
	String	title	= null;


	@SuppressWarnings("unchecked")
	public void read(String fileName)
		throws JDOMException, IOException
	{
		SAXBuilder sax = new SAXBuilder();
		Document doc = sax.build(new File(fileName));
		Element root = doc.getRootElement();
		title = root.getChildText("title");

		List<Element> sentences = root.getChild("content").getChildren();
		for( int i = 0, size = sentences.size(); i < size; i++ ) {
			add(getSentence(sentences.get(i)));
		}
	}


	@SuppressWarnings("unchecked")
	private ANSSentence getSentence(Element elmt)
	{
		ANSSentence ret = new ANSSentence();
		ret.setSentence(elmt.getChild("value").getValue());
		if( elmt.getChild("org") != null ) ret.setOrg(elmt.getChild("org").getValue());
		List<Element> eojeols = elmt.getChild("result").getChildren();
		for( int i = 0, size = eojeols.size(); i < size; i++ ) {
			ANSEojeol ej = getEojeol(eojeols.get(i));
			ej.merge();
			ret.add(ej);
		}
		return ret;
	}


	@SuppressWarnings("unchecked")
	private ANSEojeol getEojeol(Element elmt)
	{
		ANSEojeol ret = new ANSEojeol();
		ret.exp = elmt.getAttributeValue("exp");
		List<Element> mps = elmt.getChildren("morpheme");
		for( int i = 0, size = mps.size(); i < size; i++ ) {
			String src = mps.get(i).getValue();
			String string = null, tag = null;
			if( src.startsWith("/") ) {
				string = "/";
				tag = "SP";
			} else {
				try {
					String[] arrTemp = src.split("/");
					string = arrTemp[0];
					tag = arrTemp[1];
				} catch (Exception e) {
					System.err.println(src);
				}
			}
			try {
				ANSMorp mp = new ANSMorp(string, POSTag.getTagNum(tag));
				ret.add(mp);
			} catch (Exception e) {
				System.err.println(src);
				e.printStackTrace();
				System.exit(-1);
			}

		}
		return ret;
	}
}
