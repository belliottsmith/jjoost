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

import java.util.Map.Entry;

import org.jjoost.util.Function;

/**
 * This interface declares a map that permits duplicate keys <b>and</b> duplicate key->value pairs,
 * i.e. a key can map to an arbitrary combination of possibly duplicated values
 */
public interface ListReadMap<K, V> extends AnyReadMap<K, V>, Function<K, Iterable<V>> {

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#values(java.lang.Object)
	 */
	@Override public MultiReadSet<V> values(K key);

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#keys()
	 */
	@Override public MultiReadSet<K> keys();
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#entries()
	 */
	@Override public MultiReadSet<Entry<K, V>> entries();
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public ListReadMap<K, V> copy();

}
