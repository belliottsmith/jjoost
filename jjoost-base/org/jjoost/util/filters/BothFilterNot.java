package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;

/**
 * A filter negating the supplied filter implementing both <code>Filter</code> and <code>PartialOrder</code>
 * 
 * @author b.elliottsmith
 */
public class BothFilterNot<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private F negate ;

    /**
	 * Construct a new filter negating the supplied filter
	 * 
	 * @param negate
	 *            filter to negate
	 */
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
		return negate.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return negate.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	public String toString() {
		return "not " + negate;
	}

    /**
	 * Returns the negation of the supplied filter implementing
	 * 
	 * @param negate
	 *            filter to negate
	 * @return negation of the supplied filter
	 */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterNot<E, F> get(F negate) {
		return new BothFilterNot<E, F>(negate) ;
	}

}
