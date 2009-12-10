package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

public class AcceptIfNonEmptyString<E> implements Filter<E> {

	private static final long serialVersionUID = -1669760654698361893L ;

	public boolean accept(E test) {
		String str ; // cache result of toString() method to ensure we don't repeat work unnecessarily
		return test != null && (str = test.toString()) != null && str.length() > 0 ;
	}

	public String toString() {
		return "is not empty" ;
	}

}