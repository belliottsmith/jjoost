package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.Equalities ;
import org.jjoost.util.Equality ;

/**
 * Keeps only the first in a sequence of duplicates
 */
public class AcceptUniqueSequence<V> implements BothFilter<V> {

	private static final long serialVersionUID = 4135610622081116945L ;
	private final Equality<? super V> eq ;

	public AcceptUniqueSequence() {
		this(Equalities.object()) ;
	}

	public AcceptUniqueSequence(Equality<? super V> eq) {
		this.eq = eq ;
	}

	private V prev = null ;
	public boolean accept(V next) {
		final boolean r = !eq.equates(next, prev) ;
		prev = next ;
		return r ;
	}
	
	@Override
	public boolean accept(V next, Comparator<? super V> cmp) {
		final boolean r = cmp.compare(prev, next) != 0 ;
		prev = next ;
		return r ;
	}

	// would be cheaper constant factor to just return TRUE here, however for a set of repeated values this would cause every element to be visited rather than just a handful
	@Override
	public boolean mayAcceptBetween(V lb, boolean lbInclusive, V ub, boolean ubInclusive, Comparator<? super V> cmp) {
		return 	(prev == null | lb == null | ub == null | !lbInclusive | !ubInclusive) 
		||  	cmp.compare(lb, prev) != 0 || cmp.compare(ub, prev) != 0 ;
	}

	public String toString() {
		return "is not preceded by itself" ;
	}

}
