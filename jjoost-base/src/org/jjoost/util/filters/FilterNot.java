package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

/**
 * A that negates/inverts the result of the supplied filter
 * 
 * @author b.elliottsmith
 */
public class FilterNot<E> implements Filter<E> {

	private static final long serialVersionUID = 5515653420277621870L ;
	private final Filter<E> negate ;

	/**
	 * Construct a new filter which negates the results of the filter provided
	 * 
	 * @param negate filter to negate
	 */
	public FilterNot(Filter<E> negate) {
		this.negate = negate ;
	}

	public boolean accept(E test) {
		return !negate.accept(test) ;
	}

	public String toString() {
		return "is not " + negate ;
	}

	/**
	 * Returns the negation of the supplied filter
	 * 
	 * @param negate filter to negate
	 * @return negation of the supplied filter
	 */
	public static <E> FilterNot<E> get(Filter<E> negate) {
		return new FilterNot<E>(negate) ;
	}

}
