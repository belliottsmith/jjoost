/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.util.filters ;

import java.util.Comparator ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * Returns a filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
 * Each bound can be specified as inclusive or exclusive
 */
public class PartialOrderAcceptBetween<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L ;

	final E lb ;
	final E ub ;
	final int lbOffsetIfUbInclusive ;
	final int ubOffsetIfLbInclusive ;

    /**
     * Constructs a new filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
     * Each bound can be specified as inclusive or exclusive
     * 
     * @param lb
     *            lower limit of acceptable values
     * @param lbIsInclusive
     *            <code>true</code> if lb should be inclusive, <code>false</code> if exclusive
     * @param ub
     *            exclusive upper limit of acceptable values
     * @param ubIsInclusive
     *            <code>true</code> if <code>ub</code> should be inclusive, <code>false</code> if exclusive
     */
	public PartialOrderAcceptBetween(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		super() ;
		this.lb = lb ;
		this.ub = ub ;
		lbOffsetIfUbInclusive = lbIsInclusive ? 1 : 0 ;
		ubOffsetIfLbInclusive = ubIsInclusive ? 1 : 0 ;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return (lb == null || cmp.compare(test, lb) > lbOffsetIfUbInclusive)
				&& (ub == null || cmp.compare(test, ub) < ubOffsetIfLbInclusive) ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return (this.lb == null || ub == null || cmp.compare(this.lb, ub) < (lbInclusive ? lbOffsetIfUbInclusive : 0))
				&& (this.ub == null || lb == null || cmp.compare(lb, this.ub) < (ubInclusive ? ubOffsetIfLbInclusive : 0)) ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return (lb == null || (this.lb != null && cmp.compare(lb, this.lb) < (lbInclusive ? 0 : lbOffsetIfUbInclusive)))
		|| (ub == null || (this.ub != null && cmp.compare(this.ub, ub) < (ubInclusive ? 0 : ubOffsetIfLbInclusive ))) ;
	}
	
    /**
	 * Returns a partial order filter that accepts everything greater than or equal to the provided lower bound (first argument) and
	 * everything strictly less than the provided upper bound (second argument), as determined by the <code>Comparator</code> provided to
	 * its methods by utilising classes.
	 * 
	 * @param lb
	 *            inclusive lower limit of acceptable values
	 * @param ub
	 *            exclusive upper limit of acceptable values
	 * @return a filter that accepts everything in the range <code>[lb...ub)</code>
	 */
	public static <E> PartialOrderAcceptBetween<E> get(E lb, E ub) {
		return get(lb, true, ub, false) ;
	}

    /**
     * Returns a filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
     * Each bound can be specified as inclusive or exclusive
     * <p>
     * Returns an object implementing both <code>Filter</code> and <code>FilterPartialOrder</code>; the <code>Filter</code>
     * <code>accept()</code>method delegates to the object's <code>compareTo()</code> method, whereas the <code>FilterPartialOrder</code>
     * methods utilise the provided comparators
     * 
     * @param lb
     *            lower limit of acceptable values
     * @param lbIsInclusive
     *            <code>true</code> if lb should be inclusive, <code>false</code> if exclusive
     * @param ub
     *            exclusive upper limit of acceptable values
     * @param ubIsInclusive
     *            <code>true</code> if <code>ub</code> should be inclusive, <code>false</code> if exclusive
     * @return a filter that accepts everything in the range <code>[lb...ub)</code>
     */
	public static <E> PartialOrderAcceptBetween<E> get(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		return new PartialOrderAcceptBetween<E>(lb, lbIsInclusive, ub, ubIsInclusive) ;
	}

	public String toString() {
		return "is between " + lb + " and " + ub ;
	}

}
