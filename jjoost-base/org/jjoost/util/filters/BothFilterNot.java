package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;

public class BothFilterNot<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private F negate ;

	public BothFilterNot(F negate) {
		this.negate = negate ;
	}

	public boolean accept(E test) {
		return !negate.accept(test) ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		return !negate.accept(test, cmp) ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return !negate.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	public String toString() {
		return "not " + negate;
	}

	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterNot<E, F> get(F negate) {
		return new BothFilterNot<E, F>(negate) ;
	}

}
