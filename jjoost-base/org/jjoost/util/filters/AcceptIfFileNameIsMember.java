package org.jjoost.util.filters ;

import java.io.File ;
import java.io.FileFilter ;

import org.jjoost.collections.ArbitrarySet ;
import org.jjoost.util.Filter ;

public class AcceptIfFileNameIsMember implements Filter<File>, FileFilter {

	private static final long serialVersionUID = 2312285149003933324L ;

	private final boolean matchFullPath ;
	private final ArbitrarySet<String> members ;

	public AcceptIfFileNameIsMember(ArbitrarySet<String> members, boolean matchFullPath) {
		this.members = members ;
		this.matchFullPath = matchFullPath ;
	}

	public boolean accept(File test) {
		return members.contains(matchFullPath ? test.getAbsolutePath() : test.getName()) ;
	}

	public String toString() {
		return "is member of " + members.toString() ;
	}

}
