package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

public class AcceptIfNull<E> implements Filter<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	public boolean accept(E test) {
		return test == null ;
	}

	public String toString() {
		return "is null" ;
	}

}
