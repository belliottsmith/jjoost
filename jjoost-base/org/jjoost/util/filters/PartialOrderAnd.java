package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * A filter representing the conjunction (i.e. "and") of the supplied partial order filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if all previous filters passed
 * 
 * @author b.elliottsmith
 */
public class PartialOrderAnd<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	
	/**
	 * Filter applied first
	 */
	protected final FilterPartialOrder<P> a ;
	
	/**
	 * Filter applied second, if first filter passes
	 */
	protected final FilterPartialOrder<P> b ;

    /**
     * Construct a new filter representing the conjunction (i.e. "and") of the supplied partial order filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     */
	public PartialOrderAnd(FilterPartialOrder<P> a, FilterPartialOrder<P> b) {
		this.a = a ;
		this.b = b ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return a.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
		&& b.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean mayRejectBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return a.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
		|| b.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		return a.accept(test, cmp) && b.accept(test, cmp) ;
	}

    /**
     * Returns the conjunction (i.e. "and") of the supplied partial order filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return conjunction (i.e. "and") of a and b
     */
	public static <P> PartialOrderAnd<P> get(FilterPartialOrder<P> a, FilterPartialOrder<P> b) {
		return new PartialOrderAnd<P>(a, b) ;
	}

}
