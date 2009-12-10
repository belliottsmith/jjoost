package org.jjoost.util ;

import java.util.Comparator;

/**
 * <summary>
 * Using an <i>implied</i> total order and an ordered subset thereof, provides
 * a means of determining if an object is a member of the subset and if the
 * subset contains any objects within a specific range of the complete total order
 * <p>The total-ordering must not contain <code>null</code> as this is used to indicate plus <b>and</b> minus infinity.
 */

// TODO : optimise use of "inclusive"
public interface FilterPartialOrder<P> {

    boolean accept(P v, Comparator<? super P> cmp) ;
    
    /**
     * <summary>
     * null values should be seen as both +/- infinity, i.e.
     * containsBetween(null, o) should return containsBefore(o) and
     * containsBetween(o, null) should return containsAfter(o)
     * containsBetween should take arguments IN ORDER, i.e. o1 <= o2; 
     * if there exists no o3 such that o1 < o3 < o2 then containsBetween should
     * return false (i.e. containsBetween(1, 2) over the integers should return false). 
     * behaviour where o2 < o1 is undefined.
     * </summary>
     */

    boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) ;

}
