package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

public class BothFilterMultiOr<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final F[] disjoin ;

	public BothFilterMultiOr(F... conjoin) {
		this.disjoin = conjoin ;
	}

	@SuppressWarnings("unchecked")
	public BothFilterMultiOr(Iterable<? extends F> conjoin) {
		this.disjoin = (F[]) Iters.toArray(conjoin, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = false ;
		for (int i = 0 ; !r & i != disjoin.length ; i++)
			r = disjoin[i].accept(test) ;
		return r ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		boolean r = false ;
		for (int i = 0 ; !r & i != disjoin.length ; i++)
			r = disjoin[i].accept(test, cmp) ;
		return r ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean r = false ;
		for (int i = 0 ; !r & i != disjoin.length ; i++)
			r = disjoin[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return r ;
	}

	public String toString() {
		return "any hold: " + disjoin ;
	}

	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiOr<E, F> get(F ... conjoin) {
		return new BothFilterMultiOr<E, F>(conjoin) ;
	}

	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiOr<E, F> get(Iterable<? extends F> conjoin) {
		return new BothFilterMultiOr<E, F>(conjoin) ;
	}

}
