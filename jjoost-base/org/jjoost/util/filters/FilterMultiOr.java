package org.jjoost.util.filters ;

import org.jjoost.util.Filter ;
import org.jjoost.util.Iters ;

public class FilterMultiOr<E> implements Filter<E> {

	private static final long serialVersionUID = 6311808530912921895L ;
	private final Filter<? super E>[] disjoin ;

	public FilterMultiOr(Filter<? super E>... disjoin) {
		this.disjoin = disjoin ;
	}

	@SuppressWarnings("unchecked")
	public FilterMultiOr(Iterable<? extends Filter<? super E>> disjoin) {
		this.disjoin = Iters.toArray(disjoin, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = false ;
		for (int i = 0 ; !r & i != disjoin.length ; i++)
			r = disjoin[i].accept(test) ;
		return r ;
	}

	public String toString() {
		return "any hold: " + disjoin.toString() ;
	}

	public static <E> FilterMultiOr<E> get(Filter<? super E>... disjoin) {
		return new FilterMultiOr<E>(disjoin) ;
	}

	public static <E> FilterMultiOr<E> get(Iterable<? extends Filter<? super E>> disjoin) {
		return new FilterMultiOr<E>(disjoin) ;
	}

}
