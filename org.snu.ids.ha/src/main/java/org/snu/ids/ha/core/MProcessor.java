package org.snu.ids.ha.core;


import java.util.ArrayList;
import java.util.List;

import org.snu.ids.ha.constants.POSTag;


public class MProcessor
{

	public List<Sentence> divide(List<MCandidate> list)
	{
		List<Sentence> ret = new ArrayList<Sentence>();

		Sentence sent = new Sentence();
		for( int i = 0, size = list.size(); i < size; i++ ) {
			MCandidate mc = list.get(i);
			sent.add(mc);
			// 종료되는 것이 나타나면 해당 어절까지만 문장에 삽입해줌.
			if( mc.isLastTagOf(POSTag.EF) ) {
				for( i++; i < size; i++ ) {
					mc = list.get(i);
					if( mc.withSpace ) {
						ret.add(sent);
						sent = new Sentence();
						sent.add(mc);
						break;
					}
					sent.add(mc);
				}
			}
		}
		ret.add(sent);

		return ret;
	}

}
