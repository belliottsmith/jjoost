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

import org.jjoost.util.Factory;

/**
 * This class encapsulates the concept of nesting for objects that implement <code>ListMap</code>.
 * Two kinds of nesting are supported: <code>INLINE</code> and <code>NESTED</code>. <code>INLINE</code>
 * indicates that the map should be structurally the same as if it did not support duplicates,
 * only that duplicate records are handled correctly. i.e. an <code>INLINE ListMap</code> that
 * contains no duplicate keys should look structurally identical to a regular <code>Map</code>.
 * A <code>NESTED ListMap</code>, however, should store a <code>MultiSet</code> against every
 * single key. If <code>NESTED</code>, a <code>ListMapNesting</code> requires a <code>Factory<MultiSet<V>></code>
 * in order to produce each <code>MultiSet</code> we store against each key. 
 */
public class ListMapNesting<V> {

	/**
	 * An enumeration of the nesting types for ListMap
	 */
	public static enum Type {
		/**
		 * Store the values for duplicates keys simply as extra entries using the same mechanism as an equivalent Map would use to
		 * store its values. Typically this will be more efficient than <code>NESTED</code> for small numbers of duplicate keys.
		 */
		INLINE,
		/**
		 * Store the values for duplicates keys in a nested <code>MultiSet</code>. Typically this will be more efficient than
		 * <code>INLINE</code> for moderate to large average numbers of duplicate keys. 
		 */
		NESTED;
	}
	
	/**
	 * INLINE or NESTED	 * 
	 * @return the type
	 */
	public Type type() { return type ; }
	
	/**
	 * Factory; ignored if type() == INLINE
	 * 
	 * @return null if type() == INLINE, otherwise a factory
	 */
	public Factory<MultiSet<V>> factory() { return factory ; }
	
	private final Type type;
	private final Factory<MultiSet<V>> factory;
	
	/**
	 * Instantiates a new list map nesting.
	 * 
	 * @param factory the factory
	 * @param type the type
	 */
	protected ListMapNesting(Factory<MultiSet<V>> factory, Type type) {
		super();
		this.factory = factory;
		this.type = type;
	}


	/** The Constant INLINE */
	private static final ListMapNesting<?> INLINE = new ListMapNesting<Object>(null, Type.INLINE);
	
	/**
	 * Public method for retrieving the <code>ListMapNesting</code> that represents <code>INLINE</code> nesting
	 * 
	 * @return the list map nesting
	 */
	@SuppressWarnings("unchecked")
	public static <V> ListMapNesting<V> inline() {
		return (ListMapNesting<V>) INLINE;
	}
	
	/**
	 * Public method for retrieving a <code>ListMapNesting</code> to represent the required <code>NESTED</code> nesting
	 * 
	 * @param factory the factory	 * 
	 * @return the list map nesting
	 */
	public static <V> ListMapNesting<V> nested(Factory<MultiSet<V>> factory) {
		if (factory == null)
			throw new IllegalArgumentException();
		return new ListMapNesting<V>(factory, Type.NESTED);
	}
	
}
