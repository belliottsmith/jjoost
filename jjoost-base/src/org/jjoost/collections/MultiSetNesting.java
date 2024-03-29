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

package org.jjoost.collections;

/**
 * This class encapsulates the concept of nesting for objects that implement <code>MultiSet</code>.
 * Three kinds of nesting are supported: <code>INLINE</code>, <code>COUNTING</code> and <code>NESTED</code>. 
 * <code>INLINE</code> indicates that the map should be structurally the same as if it did not support duplicates,
 * only that duplicate records are handled correctly. i.e. an <code>INLINE MultiSet</code> that
 * contains no duplicate keys should look structurally identical to a regular <code>Set</code>.
 * A <code>COUNTING MutliSet</code>, however, should store an integer against each unique key, storing each
 * key just once. A <code>NESTED</code> set should store each key just once, and store a list of each duplicate
 * against that key.
 */
public class MultiSetNesting<V> {

	/**
	 * An enumeration of the nesting types for MultiSet
	 * 
	 * @author b.elliottsmith
	 */
	public static enum Type {		
		/**
		 * Store duplicate values merely as an integer against the first such value encountered
		 * This is the most efficient means of storing duplicate values.
		 */
		COUNTING, 
		/**
		 * Store duplicate values "inline" - i.e. in the same structure as we would store unique values
		 * Typically this will be more efficient than <code>NESTED</code> for very small numbers of duplicates
		 */
		INLINE,
		/**
		 * Store duplicate values in a nested set or list. 
		 * Typically this will be more efficient than <code>INLINE</code> for moderate to large numbers of duplicates (e.g. above 3 on average)
		 */
		NESTED;
	}
	
	/**
	 * Return the MultiSetNesting.Type type of this MultiSetNesting
	 * @return the MultiSetNesting.Type type of this MultiSetNesting
	 */
	public Type type() { return type ; }
	private final Type type;
	MultiSetNesting(Type type) {
		super();
		this.type = type;
	}
	
	private static final MultiSetNesting<?> INLINE = new MultiSetNesting<Object>(Type.INLINE);
	
	private static final MultiSetNesting<?> COUNTING = new MultiSetNesting<Object>(Type.COUNTING);
	
	private static final MultiSetNesting<?> NESTED = new MultiSetNesting<Object>(Type.NESTED);
	
	/**
	 * Return a MultiSetNesting whose type is INLINE
	 * @return a MultiSetNesting whose type is INLINE
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> inline() {
		return (MultiSetNesting<V>) INLINE;
	}

	/**
	 * Return a MultiSetNesting whose type is COUNTING
	 * @return a MultiSetNesting whose type is COUNTING
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> counting() {
		return (MultiSetNesting<V>) COUNTING;
	}
	
	/**
	 * Return a MultiSetNesting whose type is NESTED
	 * @return a MultiSetNesting whose type is NESTED
	 */
	@SuppressWarnings("unchecked")
	public static <V> MultiSetNesting<V> nested() {
		return (MultiSetNesting<V>) NESTED;
	}
	
}
