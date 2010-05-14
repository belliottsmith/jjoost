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
import org.jjoost.util.Function;

/**
 * This interface declares a map that permits each key to map to at most one value
 * 
 * @author b.elliottsmith
 */
public interface Map<K, V> extends ReadMap<K, V>, AnyMap<K, V> {

	/**
	 * Ensures that the provided key binds to the provided value, removing and
	 * returning the value currently associated with the key, or null if none.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	public V put(K key, V val) ;
	
	/**
	 * Attempts to bind the provided key to the provided value. If the key does not occur
	 * in the map then the value will be associated with it and null returned. If the key
	 * occurs in the map and is bound to a different value then this existing value will 
	 * be returned
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value already associated with the key in the map, or null if none
	 */
	public V putIfAbsent(K key, V val) ;
	
	/**
	 * Equivalent to <code>putIfAbsent(key, putIfNotPresent.create())</code>, except that 
	 * <code>putIfNotPresent.create()</code> is only executed if there is no key associated
	 * with the value. In concurrent maps this is not a guarantee, but a best effort,
	 * as it is possible for another thread to set a value for the key after this has executed
	 * but before the record can be inserted.
	 * 
	 * @param key the key
	 * @param putIfNotPresent the put if not present
	 * 
	 * @return the value associated with the provided key pre method
	 */
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	
	/**
	 * Equivalent to putIfAbsent(key, putIfNotPresent), except that instead of returning
	 * the value previously associated with the key, returns the value associated with the
	 * key as the method is exiting; i.e. if a new value is associated with the key as a
	 * result of this method, this new value will be returned, otherwise the existing value
	 * will be
	 * 
	 * @param key the key
	 * @param putIfNotPresent the put if not present
	 * 
	 * @return the value associated with the provided key post method
	 */
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) ;
	
	/**
	 * Equivalent to putIfAbsent(key, putIfNotPresent.create(key)), except that <br />
	 * 
	 * <ol>
	 * <li><code>putIfNotPresent.create()</code> is only executed if there is no
	 * key associated with the value. In concurrent maps this is not a
	 * guarantee, but a best effort, as it is possible for another thread to set
	 * a value for the key after this has executed but before the record can be
	 * inserted</li>
	 * <li>instead of returning the value previously associated with the key,
	 * returns the value associated with the key as the method is exiting; i.e.
	 * if a new value is associated with the key as a result of this method,
	 * this new value will be returned, otherwise the existing value will be
	 * <p></li>
	 * </ol>
	 * 
	 * @param key the key
	 * @param putIfNotPresent put if not present
	 * @return the value associated with the key post method
	 */
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	public abstract Map<K, V> copy();
	
}
