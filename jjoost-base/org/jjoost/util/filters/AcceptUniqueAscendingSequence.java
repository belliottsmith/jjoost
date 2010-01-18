package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * A partial order filter which will accept a value only if it has never previously seen a value greater than or equal to the value being tested;
 * in an ordered set this results in unique values being efficiently obtained if applied in an ascending order visit of some kind; it is a one shot filter, however,
 * given the state stored.
 * 
 * @author b.elliottsmith
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
	
	@Override
	public boolean mayRejectBetween(V lb, boolean lbInclusive, V ub, boolean ubInclusive, Comparator<? super V> cmp) {
		return !mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	public String toString() {
		return "is not preceded by itself" ;
	}
	
    /**
     * Returns a partial order filter which will accept a value only if it has never previously seen a value greater than or equal to the value being tested;
     * in an ordered set this results in unique values being efficiently obtained if applied in an ascending order visit of some kind; it is a one shot filter, however,
     * given the state stored.
     * 
     * @return a filter accepting a unique sequence if applied in ascending order
     */
	public static <V> AcceptUniqueAscendingSequence<V> get() {
		return new AcceptUniqueAscendingSequence<V>() ;
	}

}
