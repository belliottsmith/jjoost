package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * Keeps only the first in a sequence of duplicates
 */
public class AcceptUniqueAscendingSequence<V> implements FilterPartialOrder<V> {

	private static final long serialVersionUID = 4135610622081116945L ;

	private V prev = null ;
	
	@Override
	public boolean accept(V next, Comparator<? super V> cmp) {
		final boolean r = cmp.compare(prev, next) < 0 ;
		prev = next ;
		return r ;
	}

	@Override
	public boolean mayAcceptBetween(V lb, boolean lbInclusive, V ub, boolean ubInclusive, Comparator<? super V> cmp) {
		return 	(prev == null | ub == null) || cmp.compare(prev, ub) < 0 ;
	}

	public String toString() {
		return "is not preceded by itself" ;
	}
	
	public static <V> AcceptUniqueAscendingSequence<V> get() {
		return new AcceptUniqueAscendingSequence<V>() ;
	}

}
