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

import org.jjoost.util.Filter ;
import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Iters ;

/**
 * A filter representing the conjunction (i.e. "and") of the supplied filters implementing both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if all previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class BothFilterMultiAnd<E, F extends Filter<? super E> & FilterPartialOrder<E>> implements BothFilter<E> {

	private static final long serialVersionUID = 7419162471960836459L ;
	private final F[] filters ;

	/**
	 * Construct a new filter representing the conjunction (i.e. "and") of the supplied filters
	 * 
	 * @param filters filters to apply
	 */
	public BothFilterMultiAnd(F... filters) {
		this.filters = filters ;
	}

	/**
	 * Construct a new filter representing the conjunction (i.e. "and") of the supplied filters
	 * 
	 * @param filters filters to apply
	 */
	@SuppressWarnings("unchecked")
	public BothFilterMultiAnd(Iterable<? extends F> filters) {
		this.filters = (F[]) Iters.toArray(filters, Filter.class) ;
	}

	public boolean accept(E test) {
		boolean r = true ;
		for (int i = 0 ; r & i != filters.length ; i++)
			r = filters[i].accept(test) ;
		return r ;
	}

	public boolean accept(E test, Comparator<? super E> cmp) {
		boolean r = true ;
		for (int i = 0 ; r & i != filters.length ; i++)
			r = filters[i].accept(test, cmp) ;
		return r ;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean r = true ;
		for (int i = 0 ; r & i != filters.length ; i++)
			r = filters[i].mayAcceptBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return r ;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		boolean result = false ;
		for (int i = 0 ; !result & i != filters.length ; i++)
			result = filters[i].mayRejectBetween(lb, lbInclusive, ub, ubInclusive, cmp) ;
		return result ;
	}
	
	public String toString() {
		return "all hold: " + filters ;
	}

    /**
	 * Returns the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     * @return conjunction (i.e. "and") of provided filters
     */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiAnd<E, F> get(F ... filters) {
		return new BothFilterMultiAnd<E, F>(filters) ;
	}

    /**
	 * Returns the conjunction (i.e. "and") of the supplied filters
     * 
     * @param filters filters to apply
     * @return conjunction (i.e. "and") of provided filters
     */
	public static <E, F extends Filter<? super E> & FilterPartialOrder<E>> BothFilterMultiAnd<E, F> get(Iterable<? extends F> filters) {
		return new BothFilterMultiAnd<E, F>(filters) ;
	}

}
