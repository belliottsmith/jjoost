package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Iters ;

public class FilterMultiAnd<E> implements Filter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final Filter<? super E>[] conjoin ;

	public FilterMultiAnd(Filter<? super E>... conjoin) {
		this.conjoin = conjoin ;
	}

	@SuppressWarnings("unchecked")
	public FilterMultiAnd(Iterable<? extends Filter<? super E>> conjoin) {
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

	public static <E> FilterMultiAnd<E> get(Filter<? super E>... conjoin) {
		return new FilterMultiAnd<E>(conjoin) ;
	}

	public static <E> FilterMultiAnd<E> get(Iterable<? extends Filter<? super E>> conjoin) {
		return new FilterMultiAnd<E>(conjoin) ;
	}

}
