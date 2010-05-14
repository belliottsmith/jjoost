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
 * This interface declares a map that permits duplicate keys <b>and</b> duplicate key->value pairs,
 * i.e. a key can map to an arbitrary combination of possibly duplicated values
 */
public interface ListMap<K, V> extends AnyMap<K, V>, ListReadMap<K, V> {

	/**
	 * Appends the provided key->value pair to the map; if equal pairs already exist
	 * they are <b>not</b> overridden, but co-exist with the new pair
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return null
	 */
	@Override public V put(K key, V val) ;

	/**
	 * Appends the provided key->value pair to the map <b>if no pair exists where both
	 * key and value are equal to the one provided</b>; otherwise the value of the first 
	 * equal pair encountered is returned and the map is not modified
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return value of any existing pair where both key and value are equal
	 */
	@Override public V putIfAbsent(K key, V val) ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public ListMap<K, V> copy() ;

}
