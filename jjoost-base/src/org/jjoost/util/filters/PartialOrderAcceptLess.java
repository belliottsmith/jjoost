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
 * A partial order filter that accepts everything less than the provided value
 */
public class PartialOrderAcceptLess<E> implements FilterPartialOrder<E> {

	private static final long serialVersionUID = 1064862673649778571L;
	final E than;

    /**
	 * Constructs a new partial order filter that accepts everything less than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            exclusive upper limit of acceptable values
	 */
	public PartialOrderAcceptLess(E than) {
		super();
		this.than = than;
	}

	@Override
	public boolean accept(E test, Comparator<? super E> cmp) {
		return cmp.compare(test, than) < 0;
	}

	@Override
	public boolean mayAcceptBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		return lb == null || cmp.compare(lb, than) < 0;
	}
	
	@Override
	public boolean mayRejectBetween(E lb, boolean lbInclusive, E ub, boolean ubInclusive, Comparator<? super E> cmp) {
		final int offset = ubInclusive ? -1 : 0;
		return ub == null || cmp.compare(than, ub) > offset;
	}

    /**
	 * Returns a partial order filter that accepts everything less than the provided value as determined by the <code>Comparator</code>
	 * provided to its methods by utilising classes
	 * 
	 * @param than
	 *            exclusive upper limit of acceptable values
	 * @return a partial order filter that accepts everything less than the provided value
	 */
	public static <E> PartialOrderAcceptLess<E> get(E than) {
		return new PartialOrderAcceptLess<E>(than);
	}

	public String toString() {
		return "is less than " + than;
	}

}
