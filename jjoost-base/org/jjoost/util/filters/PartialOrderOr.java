package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * A filter representing the disjunction (i.e. "or") of the supplied partial order filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if not previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class PartialOrderOr<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	
	/**
	 * Filter applied first
	 */
	protected final FilterPartialOrder<P> a ;
	
	/**
	 * Filter applied second, if first filter does not pass
	 */
	protected final FilterPartialOrder<P> b ;

    /**
     * Construct a new filter representing the disjunction (i.e. "or") of the supplied partial order filters.
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     */
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
	public boolean mayRejectBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return a.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
		&& b.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		return a.accept(test, cmp) || b.accept(test, cmp) ;
	}

    /**
     * Returns the disjunction (i.e. "or") of the supplied partial order filters.
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return disjunction (i.e. "or") of a and b
     */
	public static <P> PartialOrderOr<P> get(FilterPartialOrder<P> a, FilterPartialOrder<P> b) {
		return new PartialOrderOr<P>(a, b) ;
	}

}
