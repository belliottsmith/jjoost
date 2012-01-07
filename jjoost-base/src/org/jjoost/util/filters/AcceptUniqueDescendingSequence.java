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

import java.util.Comparator;

import org.jjoost.util.FilterPartialOrder;

/**
 * A partial order filter which will accept a value only if it has never previously seen a value less than or equal to the value being tested;
 * in an ordered set this results in unique values being efficiently obtained if applied in an descending order visit of some kind; it is a one shot filter, however,
 * given the state stored.
 * 
 * @author b.elliottsmith
 */
public class AcceptUniqueDescendingSequence<V> implements FilterPartialOrder<V> {

	private static final long serialVersionUID = 4135610622081116945L;

	private V prev = null;
	
	@Override
	public boolean accept(V next, Comparator<? super V> cmp) {
		final boolean r = cmp.compare(prev, next) > 0;
		prev = next;
		return r;
	}

	@Override
	public boolean mayAcceptBetween(V lb, boolean lbInclusive, V ub, boolean ubInclusive, Comparator<? super V> cmp) {
		return 	(prev == null | lb == null) || cmp.compare(lb, prev) < 0;
	}

	@Override
	public boolean mayRejectBetween(V lb, boolean lbInclusive, V ub, boolean ubInclusive, Comparator<? super V> cmp) {
		return !mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp);
	}
	
	public String toString() {
		return "is not preceded by itself";
	}
	
    /**
     * Returns a partial order filter which will accept a value only if it has never previously seen a value less than or equal to the value being tested;
     * in an ordered set this results in unique values being efficiently obtained if applied in an descending order visit of some kind; it is a one shot filter, however,
     * given the state stored.
     * 
     * @return a filter accepting a unique sequence if applied in descending order
     */
	public static <V> AcceptUniqueDescendingSequence<V> get() {
		return new AcceptUniqueDescendingSequence<V>();
	}

}
