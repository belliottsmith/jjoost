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

package org.jjoost.util.filters;

/**
 * A filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
 * Each bound can be specified as inclusive or exclusive
 */
public class AcceptBetween<E extends Comparable<? super E>> extends PartialOrderAcceptBetween<E> implements BothFilter<E> {

	private static final long serialVersionUID = 1064862673649778571L;

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
	public AcceptBetween(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		super(lb, lbIsInclusive, ub, ubIsInclusive);
	}

	@Override
	public boolean accept(E test) {
		return (lb == null || lb.compareTo(test) < lbOffsetIfUbInclusive)
			&& (ub == null || test.compareTo(ub) < ubOffsetIfLbInclusive);
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
	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, E ub) {
		return get(lb, true, ub, false);
	}

    /**
     * Returns a filter that accepts everything between the provided lower and upper bounds, as determined by the <code>Comparator</code> provided to its methods. 
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
     * @return a filter that accepts everything in the range <code>[lb...ub)</code>
     */
	public static <E extends Comparable<? super E>> AcceptBetween<E> get(E lb, boolean lbIsInclusive, E ub, boolean ubIsInclusive) {
		return new AcceptBetween<E>(lb, lbIsInclusive, ub, ubIsInclusive);
	}

	public String toString() {
		return "is between " + lb + " and " + ub;
	}

}
