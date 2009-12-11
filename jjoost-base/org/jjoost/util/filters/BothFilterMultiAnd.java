package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

public class BothFilterMultiAnd<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final F[] conjoin ;

	public BothFilterMultiAnd(F... conjoin) {
		this.conjoin = conjoin ;
	}

	@SuppressWarnings("unchecked")
	public BothFilterMultiAnd(Iterable<? extends F> conjoin) {
		this.conjoin = (F[]) Iters.toArray(conjoin, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = true ;
		for (int i = 0 ; r & i != conjoin.length ; i++)
			r = conjoin[i].accept(test) ;
		return r ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		boolean r = true ;
		for (int i = 0 ; r & i != conjoin.length ; i++)
			r = conjoin[i].accept(test, cmp) ;
		return r ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean r = true ;
		for (int i = 0 ; r & i != conjoin.length ; i++)
			r = conjoin[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return r ;
	}

	public String toString() {
		return "all hold: " + conjoin ;
	}

	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiAnd<E, F> get(F ... conjoin) {
		return new BothFilterMultiAnd<E, F>(conjoin) ;
	}

	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiAnd<E, F> get(Iterable<? extends F> conjoin) {
		return new BothFilterMultiAnd<E, F>(conjoin) ;
	}

}
