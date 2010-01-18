package org.jjoost.util ;

import java.util.Comparator;

/**
 * This interface is used to define an abstract filter over a data set whose type is known but whose
 * ordering may not be. With a provided <code>Comparator</code> instances of this interface should be able to
 * indicate whether or not a range bounds a value that the filter accepts. Null values cannot be part of the
 * ordering, as they indicate +/- infinite to the <code>mayAcceptBetween()</code> method
 * 
 * @author b.elliottsmith
 */
public interface FilterPartialOrder<P> {

	/**
     * Return true iff this filter and comparator combination accept the provided value
     * 
     * @param v the value to check
     * @param cmp the partial order
     * 
     * @return true, if accepts
     */
    boolean accept(P v, Comparator<? super P> cmp) ;
    
    /**
     * Returns true if there exists (in the total order defined by the comparator, not necessarily in any concrete 
     * data set this is being applied to) a value between <code>lb</code> and <code>ub</code> that this filter may accept.
     * This method may return true if there is no such value, at the cost of more expensive execution but valid behaviour,
     * however it cannot return false if there <b>is</b> such a value without breaking functionality.
     * 
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
     * @return true, if there may exists a value V such that lb <(/<=) V <(/<=) ub and accept(V, cmp) == true
     */

    boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) ;

    /**
     * Returns true if there exists (in the total order defined by the comparator, not necessarily in any concrete 
     * data set this is being applied to) a value between <code>lb</code> and <code>ub</code> that this filter may reject.
     * This method may return true if there is no such value at the cost of more expensive execution but valid behaviour,
     * however it cannot return false if there <b>is</b> such a value without breaking functionality.
     * 
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
     * @return true, if there may exists a value V such that lb <(/<=) V <(/<=) ub and accept(V, cmp) == false
     */
    
    boolean mayRejectBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) ;
    
}
