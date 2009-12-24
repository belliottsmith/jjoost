package org.jjoost.util ;

import java.util.Comparator;

// TODO: Auto-generated Javadoc
/**
 * This interface is used to define an abstract filter over a dataset whose datatype is known but whose
 * ordering may not be. With a provided <code>Comparator</code> instances of this interface should be able to
 * indicate whether or not a range bounds a value that the filter accepts
 * <p>This class must treat null as both plus <b>and</b> minus infinity.
 * 
 * @author b.elliottsmith
 */
public interface FilterPartialOrder<P> {

	/**
     * Return true if this filter and comparator combination accept the provided value
     * 
     * @param v the value to check
     * @param cmp the partial order
     * 
     * @return true, if accepts
     */
    boolean accept(P v, Comparator<? super P> cmp) ;
    
    /**
     * null values should be seen as both +/- infinity, i.e.
     * containsBetween(null, o) should return containsBefore(o) and
     * containsBetween(o, null) should return containsAfter(o)
     * containsBetween should take arguments IN ORDER, i.e. o1 <= o2;
     * behaviour where o2 < o1 is undefined.
     * 
     * @param lb the lower bound of the range to check
     * @param lbInclusive if the lower bound should be taken as inclusive
     * @param ub the upper bound of the range to check
     * @param ubInclusive if the upper bound should be taken as inclusive
     * @param cmp the partial order
     * 
     * @return true, if there may exists a value V such that lb <(/<=) V <(/<=) ub
     */

    boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) ;

}
