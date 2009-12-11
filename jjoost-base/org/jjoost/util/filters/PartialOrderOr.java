package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

public class PartialOrderOr<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	protected final FilterPartialOrder<P> a, b ;

	public PartialOrderOr(FilterPartialOrder<P> a, FilterPartialOrder<P> b) {
		this.a = a ;
		this.b = b ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return a.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
		|| b.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		return a.accept(test, cmp) || b.accept(test, cmp) ;
	}

	public static <P> PartialOrderOr<P> get(FilterPartialOrder<P> a, FilterPartialOrder<P> b) {
		return new PartialOrderOr<P>(a, b) ;
	}

}
