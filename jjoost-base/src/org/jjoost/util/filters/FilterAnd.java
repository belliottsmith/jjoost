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

import org.jjoost.util.Filter;

/**
 * A filter representing the conjunction (i.e. "and") of the supplied filters.
 * The filters are evaluated in the order they are provided (left-to-right) and are evaluated if and only if all previous filters passed.
 * 
 * @author b.elliottsmith
 */
public class FilterAnd<E> implements Filter<E> {

	private static final long serialVersionUID = 7419162471960836459L;
	private final Filter<? super E> a, b;

    /**
     * Construct a filter representing the conjunction (i.e. "and") of the supplied filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     */
	public FilterAnd(Filter<? super E> a, Filter<? super E> b) {
		this.a = a;
		this.b = b;
	}

	public boolean accept(E test) {
		return a.accept(test) && b.accept(test);
	}

	public String toString() {
		return a + " and " + b;
	}

    /**
     * Returns the conjunction (i.e. "and") of the supplied filters
     * 
     * @param a filter to apply first
     * @param b filter to apply second
     * @return conjunction (i.e. "and") of a and b
     */
	public static <E> FilterAnd<E> get(Filter<? super E> a, Filter<? super E> b) {
		return new FilterAnd<E>(a, b);
	}

}
