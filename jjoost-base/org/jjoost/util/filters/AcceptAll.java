package org.jjoost.util.filters ;

import java.util.Comparator ;

/**
 * A filter that accepts everything (i.e. returns true for all input). 
 * Implements both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * 
 * @author b.elliottsmith
 */
public class AcceptAll<E> implements BothFilter<E> {

	private static final long serialVersionUID = 3620521225513318797L ;

	@SuppressWarnings("unchecked")
	private static final AcceptAll INSTANCE = new AcceptAll() ;
	
	/**
	 * Return the global instance of this filter
	 * @return the global instance of AcceptAll
	 */
	@SuppressWarnings("unchecked")
	public static <E> AcceptAll<E> get() {
		return INSTANCE ;
	}

	@Override
	public boolean accept(E o) {
		return true ;
	}

	@Override
	public boolean accept(E o, Comparator<? super E> cmp) {
		return true ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return true ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return false ;
	}
	
}
