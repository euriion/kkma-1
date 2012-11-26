package org.snu.ids.ha.test;


import java.util.ArrayList;
import java.util.List;


public class ANSSentence
	extends ArrayList<ANSEojeol>
{
	private String	sentence	= null;
	private String	org			= null;


	public ANSSentence()
	{
		super();
	}


	/**
	 * @return the sentence
	 */
	public String getSentence()
	{
		return sentence;
	}


	public void setSentence(String sentence)
	{
		this.sentence = sentence;
	}


	public String getOrg()
	{
		return org;
	}


	public void setOrg(String org)
	{
		this.org = org;
	}


	public String getUnspaced()
	{
		StringBuffer sb = new StringBuffer();
		char ch1 = 0, ch2 = 0, ch3 = 0;
		sb.append(org.charAt(0));
		for( int i = 1; i < org.length() - 1; i++ ) {
			ch1 = org.charAt(i - 1);
			ch2 = org.charAt(i);
			ch3 = org.charAt(i + 1);

			if( ch2 == ' ' ) {
				Character.UnicodeBlock ub1 = Character.UnicodeBlock.of(ch1);
				Character.UnicodeBlock ub3 = Character.UnicodeBlock.of(ch3);
				if( (ub1 == Character.UnicodeBlock.HANGUL_SYLLABLES || ub1 == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) && (ub3 == Character.UnicodeBlock.HANGUL_SYLLABLES || ub3 == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) ) {
					// do nothing
				} else {
					sb.append(' ');
				}
			} else {
				sb.append(ch2);
			}
		}
		sb.append(org.charAt(org.length() - 1));
		return sb.toString();
	}


	public List<ANSMorp> getSimpleResult()
	{
		ArrayList<ANSMorp> ret = new ArrayList<ANSMorp>();
		for( ANSEojeol ej : this ) {
			ret.addAll(ej);
		}
		return ret;
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for( ANSEojeol ej : this ) {
			if( !isFirst ) {
				sb.append(" ");
			}
			sb.append(ej);
			isFirst = false;
		}
		return sb.toString();
	}
}
