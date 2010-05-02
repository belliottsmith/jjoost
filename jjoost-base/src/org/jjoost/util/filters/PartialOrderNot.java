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
 * A that negates/inverts the results of the supplied filter. Users should be aware that filters that do not accurately implement
 * <code>mayAcceptBetween</code> may break on negation.
 * 
 * @author b.elliottsmith
 */
public class PartialOrderNot<P> implements FilterPartialOrder<P> {

	private static final long serialVersionUID = 454908176068653901L ;
	
	/**
	 * filter we are negating
	 */
	protected final FilterPartialOrder<P> negate ;

	/**
	 * Construct a new filter which negates the results of the filter provided
	 * 
	 * @param negate filter to negate
	 */
	public PartialOrderNot(FilterPartialOrder<P> negate) {
		this.negate = negate ;
	}

	@Override
	public boolean mayAcceptBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return negate.mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}

	@Override
	public boolean mayRejectBetween(P lb, boolean lbInclusive, P ub, boolean ubInclusive, Comparator<? super P> cmp) {
		return negate.mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
	}
	
	@Override
	public boolean accept(P test, Comparator<? super P> cmp) {
		return !negate.accept(test, cmp) ;
	}

    /**
	 * Returns the negation of the supplied partial order filter
	 * 
	 * @param negate filter to negate
	 * @return negation of the supplied partial order filter
     */
	public static <P> PartialOrderNot<P> get(FilterPartialOrder<P> negate) {
		return new PartialOrderNot<P>(negate) ;
	}

}
