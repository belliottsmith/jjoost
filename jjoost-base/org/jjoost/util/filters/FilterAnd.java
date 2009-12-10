package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Iters ;

public class FilterAnd<E> implements Filter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final Filter<? super E>[] conjoin ;

	public FilterAnd(Filter<? super E>... conjoin) {
		this.conjoin = conjoin ;
	}

	@SuppressWarnings("unchecked")
	public FilterAnd(Iterable<? extends Filter<? super E>> conjoin) {
		this.conjoin = Iters.toArray(conjoin, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = true ;
		for (int i = 0 ; r & i != conjoin.length ; i++)
			r = conjoin[i].accept(test) ;
		return r ;
	}

	public String toString() {
		return "all hold: " + conjoin ;
	}

	public static <E> FilterAnd<E> get(Filter<? super E>... conjoin) {
		return new FilterAnd<E>(conjoin) ;
	}

	public static <E> FilterAnd<E> get(Iterable<? extends Filter<? super E>> conjoin) {
		return new FilterAnd<E>(conjoin) ;
	}

}
