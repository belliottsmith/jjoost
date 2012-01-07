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
 * This interface represents the rather dubious concept of a set that may
 * contain at most one item. This set is primarily intended for use by
 * the method <code>values(key)</code> on regular (scalar) maps.
 * 
 * @author b.elliottsmith
 */
public interface UnitarySet<V> extends UnitaryReadSet<V>, AnySet<V> {

	/**
	 * Insert the parameter into the set, removing and returning the currently value that exists in the set, if any
	 * 
	 * @param value
	 *            value to insert
	 * @return set's previous value, if any
	 */
	@Override public V put(V value);
	
	/**
	 * Returns a copy of the set. This method may or may not return a set of the same class as the one it was
	 * called on, however will return a <code>UnitarySet</code>
	 */
	@Override public UnitarySet<V> copy();
	
	/**
	 * Returns <code>this</code>
	 * 
	 * @return <code>this</code>
	 */
	public UnitarySet<V> unique();

}
