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

import org.jjoost.util.FilterPartialOrder;
import org.jjoost.util.tuples.Pair;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 * 
 * @author b.elliottsmith
 *
 * @param <V>
 */
public interface OrderedReadSet<V> extends AnyReadSet<V> {

	public V first();
	public V last();
	
	public V last(V find);

	public V first(FilterPartialOrder<V> filter);
	public V last(FilterPartialOrder<V> filter);

	public V floor(V find);
	public V ceil(V find);
	
	public V lesser(V find);
	public V greater(V find);
	
	/**
	 * find the values closest to the provided key if it does not exist, or the lowest and greatest value of the key if it does
 	 * the first argument is always the lesser of the two;
	 * is equivalent to a call to both ceil() and floor(), however the ordering of the results differs on if the key is present or not;
	 * if it is then it is equivalent to (floor, ceil); if it is not then it is equivalent to (ceil, floor). this is so that the first value is always less than the second
	 * @param find
	 * @return
	 */ 
	public Pair<V, V> boundaries(V find);

	// LAZY
	public Iterable<V> all(boolean asc);
	public Iterable<V> all(V value, boolean asc);
	public Iterable<V> unique(boolean asc);
	public OrderedSet<V> filter(FilterPartialOrder<V> filter);

	// EAGER
	public int count(FilterPartialOrder<V> filter);
	public OrderedReadSet<V> filterCopy(FilterPartialOrder<V> filter);
	public OrderedReadSet<V> copy();

}
