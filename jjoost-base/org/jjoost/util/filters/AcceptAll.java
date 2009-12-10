package org.jjoost.util.filters ;

import java.util.Comparator ;

/**
 * A simple filter that accepts everything
 * 
 * @author Benedict Elliott Smith
 * @param <E>
 */
public class AcceptAll<E> implements BothFilter<E> {

	private static final long serialVersionUID = 3620521225513318797L ;

	@SuppressWarnings("unchecked")
	private static final AcceptAll INSTANCE = new AcceptAll() ;
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

}
