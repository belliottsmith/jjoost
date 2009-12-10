package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

public class FilterNot<E> implements Filter<E> {

	private static final long serialVersionUID = 5515653420277621870L ;
	private final Filter<E> negate ;

	public FilterNot(Filter<E> negate) {
		this.negate = negate ;
	}

	public boolean accept(E test) {
		return !negate.accept(test) ;
	}

	public String toString() {
		return "is not " + negate ;
	}

	public static <E> FilterNot<E> get(Filter<E> negate) {
		return new FilterNot<E>(negate) ;
	}

}
