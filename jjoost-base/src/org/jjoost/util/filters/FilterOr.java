package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

/**
 * A filter representing the disjunction (i.e. "or") of the supplied filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if no previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class FilterOr<E> implements Filter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final Filter<? super E> a, b;

    /**
     * Construct a filter representing the disjunction (i.e. "or") of the supplied filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     */
	public FilterOr(Filter<? super E> a, Filter<? super E> b) {
		this.a = a ;
		this.b = b ;
	}

	public boolean accept(E test) {
		return a.accept(test) || b.accept(test) ;
	}

	public String toString() {
		return a + " and " + b ;
	}

    /**
     * Returns the disjunction (i.e. "or") of the supplied filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return disjunction (i.e. "or") of a and b
     */
	public static <E> FilterOr<E> get(Filter<? super E> a, Filter<? super E> b) {
		return new FilterOr<E>(a, b) ;
	}

}
