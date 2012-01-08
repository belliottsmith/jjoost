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

package org.jjoost.util;

import java.util.Comparator;

/**
 * Defines a some simple utility methods for Objects
 * 
 * @author b.elliottsmith
 */
public class Objects {

	private static final Object INITIALISATION_SENTINEL_WITH_OBJECT_ERASURE_SENTINEL = new Object();
	@SuppressWarnings("unchecked")
	public static <E> E initialisationSentinelWithObjectErasure() {
		return (E) INITIALISATION_SENTINEL_WITH_OBJECT_ERASURE_SENTINEL;
	}
	public static final boolean isInitialisationSentinelWithObjectErasure(Object o) {
		return o == INITIALISATION_SENTINEL_WITH_OBJECT_ERASURE_SENTINEL;
	}
	
    /**
	 * Perform simple object equality on the arguments without throwing an error if either argument is <code>null</code>
	 * 
	 * @param a
	 *            an object
	 * @param b
	 *            an object
	 * @return <code>true</code> if <code>a</code> and <code>b</code> are equal, <code>false</code> otherwise
	 */
    public static boolean equalQuick(final Object a, final Object b) {
    	return a == b || ((a != null & b != null) && a.equals(b));
    }
    
	@SuppressWarnings("rawtypes")
	private static final Comparator COMPARABLE_COMPARATOR = new Comparator<Comparable>() {
		@SuppressWarnings("unchecked")
		@Override
		public int compare(Comparable o1, Comparable o2) {
			return o1.compareTo(o2);
		}
	};
	
	/**
	 * Return a <code>Comparator</code> which delegates to a <code>Comparable</code> object's <code>compareTo()</code> method
	 * 
	 * @return a <code>Comparator</code> which delegates to a <code>Comparable</code> object's <code>compareTo()</code> method
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Comparable<E>> Comparator<E> getComparableComparator() {
		return (Comparator<E>) COMPARABLE_COMPARATOR;
	}

	/**
	 * Convert the provided argument to a <code>String</code>, return "null" of the argument is <code>null</code>
	 * 
	 * @param o
	 *            an object
	 * @return <code>o == null ? "null" : o.toString()</code>
	 */
	public static String toString(Object o) {
		return o == null ? "null" : o.toString();
	}
	
}
