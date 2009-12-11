package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;

public class FilterOr<E> implements Filter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final Filter<? super E> a, b;

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

	public static <E> FilterOr<E> get(Filter<? super E> a, Filter<? super E> b) {
		return new FilterOr<E>(a, b) ;
	}

}
