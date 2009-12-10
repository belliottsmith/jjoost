package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

public class AcceptIfNotNull<E> implements Filter<E> {

	private static final long serialVersionUID = -6305241848003882379L ;

	public boolean accept(E test) {
		return test != null ;
	}

	public String toString() {
		return "is not null" ;
	}

}
