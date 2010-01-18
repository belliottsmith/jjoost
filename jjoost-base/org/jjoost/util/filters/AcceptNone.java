package org.jjoost.util.filters ;

import java.util.Comparator ;

/**
 * A filter that accepts nothing (i.e. returns false for all input).
 * Implements both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * 
 * @author b.elliottsmith
 */
public class AcceptNone<E> implements BothFilter<E> {

	private static final long serialVersionUID = 3620521225513318797L ;

	@SuppressWarnings("unchecked")
	private static final AcceptNone INSTANCE = new AcceptNone() ;

	/**
	 * Return the global instance of this filter
	 * @return the global instance of AcceptNone
	 */
	@SuppressWarnings("unchecked")
	public static <E> AcceptNone<E> get() {
		return INSTANCE ;
	}

	@Override
	public boolean accept(E o) {
		return false ;
	}

	@Override
	public boolean accept(E o, Comparator<? super E> cmp) {
		return false ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return false ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return true ;
	}
	
}
