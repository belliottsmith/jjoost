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

/**
 * A filter that accepts everything (i.e. returns true for all input). 
 * Implements both <code>Filter</code> and <code>FilterPartialOrder</code>.
 * 
 * @author b.elliottsmith
 */
public class AcceptAll<E> implements BothFilter<E> {

	private static final long serialVersionUID = 3620521225513318797L;

	@SuppressWarnings("rawtypes")
	private static final AcceptAll INSTANCE = new AcceptAll();
	
	/**
	 * Return the global instance of this filter
	 * @return the global instance of AcceptAll
	 */
	@SuppressWarnings("unchecked")
	public static <E> AcceptAll<E> get() {
		return INSTANCE;
	}

	@Override
	public boolean accept(E o) {
		return true;
	}

	@Override
	public boolean accept(E o, Comparator<? super E> cmp) {
		return true;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return true;
	}

	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return false;
	}
	
}
