package org.snu.ids.ha.core;


import java.util.ArrayList;


public class CandidateList
	extends ArrayList<MCandidate>
{
	public CandidateList()
	{
		super();
	}


	public CandidateList(MCandidate mc)
	{
		super();
		add(mc);
	}


	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for( MCandidate mc : this ) {
			sb.append("\n\t" + mc);
		}
		return sb.toString();
	}
}
