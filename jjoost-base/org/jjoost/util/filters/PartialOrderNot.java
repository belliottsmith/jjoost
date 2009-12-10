package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderNot<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	protected final FilterPartialOrder<P> negate ;

	public PartialOrderNot(FilterPartialOrder<P> negate) {
		this.negate = negate ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return !negate.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		return !negate.accept(test, cmp) ;
	}

	public static <P> PartialOrderNot<P> get(FilterPartialOrder<P> negate) {
		return new PartialOrderNot<P>(negate) ;
	}

}
