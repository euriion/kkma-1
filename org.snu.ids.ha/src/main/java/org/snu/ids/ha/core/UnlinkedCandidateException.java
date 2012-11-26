package org.snu.ids.ha.core;


public class UnlinkedCandidateException
	extends Exception
{
	public Token			token		= null;
	public MCandidate		mc			= null;
	public CandidateList	prevCands	= null;


	public UnlinkedCandidateException(Token token, MCandidate mc, CandidateList prevCands)
	{
		this.token = token;
		this.mc = mc;
		this.prevCands = prevCands;
	}
}
