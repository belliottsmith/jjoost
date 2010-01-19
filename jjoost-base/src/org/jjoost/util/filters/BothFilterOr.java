package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;

/**
 * A filter representing the disjunction (i.e. "or") of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if no previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class BothFilterOr<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final F a, b;

	/**
	 * Construct a new filter representing the disjunction (i.e. "or") of the supplied filters
	 * 
	 * @param a
	 *            filter to apply first
	 * @param b
	 *            filter to apply second
	 */
	public BothFilterOr(F a, F b) {
		this.a = a ;
		this.b = b ;
	}

	public boolean accept(E test) {
		return a.accept(test) || b.accept(test) ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		return a.accept(test, cmp) || b.accept(test, cmp) ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return a.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
			|| b.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return a.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) 
		&& b.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	public String toString() {
		return a + " and " + b ; 
	}

    /**
	 * Returns the disjunction (i.e. "or") of the supplied filters
	 * 
	 * @param a
	 *            filter to apply first
	 * @param b
	 *            filter to apply second
	 * @return disjunction (i.e. "or") of a and b
	 */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterOr<E, F> get(F a, F b) {
		return new BothFilterOr<E, F>(a, b) ;
	}

}
