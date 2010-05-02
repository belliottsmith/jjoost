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

import org.jjoost.util.Filter ;

/**
 * A that negates/inverts the result of the supplied filter
 * 
 * @author b.elliottsmith
 */
public class FilterNot<E> implements Filter<E> {

	private static final long serialVersionUID = 5515653420277621870L ;
	private final Filter<E> negate ;

	/**
	 * Construct a new filter which negates the results of the filter provided
	 * 
	 * @param negate filter to negate
	 */
	public FilterNot(Filter<E> negate) {
		this.negate = negate ;
	}

	public boolean accept(E test) {
		return !negate.accept(test) ;
	}

	public String toString() {
		return "is not " + negate ;
	}

	/**
	 * Returns the negation of the supplied filter
	 * 
	 * @param negate filter to negate
	 * @return negation of the supplied filter
	 */
	public static <E> FilterNot<E> get(Filter<E> negate) {
		return new FilterNot<E>(negate) ;
	}

}
