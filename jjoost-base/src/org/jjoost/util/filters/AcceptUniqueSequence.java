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

import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Filter;
import org.jjoost.util.Objects;

/**
 * A <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
 * being tested, using the provided definition of equality.
 */
public class AcceptUniqueSequence<V> implements Filter<V> {

	private static final long serialVersionUID = 4135610622081116945L;
	private final Equality<? super V> eq;

    /**
     * Construct a new <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using regular object equality.
     */
	public AcceptUniqueSequence() {
		this(Equalities.object());
	}

    /**
     * Construct a new <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using the provided definition of equality.
     */
	public AcceptUniqueSequence(Equality<? super V> eq) {
		this.eq = eq;
	}

	private V prev = Objects.initialisationSentinelWithObjectErasure();
	public boolean accept(V next) {
		final boolean r = 
			Objects.isInitialisationSentinelWithObjectErasure(prev) 
			|| !eq.equates(next, prev);
		prev = next;
		return r;
	}
	
	public String toString() {
		return "is not preceded by itself";
	}

    /**
     * Returns a <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
     * being tested, using regular object equality.
     * 
     * @return a filter rejecting any values equal to their predecessor
     */
	public static <V> AcceptUniqueSequence<V> get() {
		return new AcceptUniqueSequence<V>();
	}
	
	/**
	 * Returns a <code>Filter</code> which returns <code>true</code> if and only if the previously tested value is not equal to the value
	 * being tested, using the provided definition of equality.
	 * 
	 * @return a filter rejecting any values equal to their predecessor
	 */
	public static <V> AcceptUniqueSequence<V> get(Equality<? super V> eq) {
		return new AcceptUniqueSequence<V>(eq);
	}
	
}
