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
 * This interface declares a map that permits duplicate keys, but no duplicate key->value pairs,
 * i.e. a key can map to an arbitrary set of values wherein no value occurs more than once
 */
public interface MultiMap<K, V> extends AnyMap<K, V>, MultiReadMap<K, V> {

	/**
	 * Ensures that the provided key binds to the provided value, removing any existing 
	 * key->value pair where the key is equal to the one provided. If any pair is removed 
	 * as a result of this action, the value of that pair is returned.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	@Override public V put(K key, V val) ;
	
	/**
	 * Attempts to bind the provided key to the provided value. If the key->value pair already 
	 * occurs in the map then the value of this pair is returned and the map is not modified,
	 * otherwise the provided pair is inserted and null returned
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	@Override public V putIfAbsent(K key, V val) ;

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public MultiMap<K, V> copy() ;

}
