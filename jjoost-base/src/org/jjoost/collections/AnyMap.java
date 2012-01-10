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

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * This interface is the common ancestor of all modifiable Jjoost maps. The methods 
 * declared here make no assumptions about the number of occurrences
 * of a given key or key->value pair.
 * <p>
 * WARNING: Note that in a <code>MultiMap</code> and <code>ListMap</code> the
 * <code>keys()</code> method returns a <code>MultiSet</code> and as such an
 * <code>Iterator</code> over this will yield each duplicate key the number of
 * times it occurs in the map. For unique keys, call
 * <code>keys().unique()</code>
 * <p>
 * Also note that an <code>Iterator</code> returned by concurrent implementors
 * of this class is permitted to return values more times than they actually
 * ever occurred <b>if a valid sequence of deletes and inserts happens</b> to
 * cause the <code>Iterator</code> to see the values multiple times. See the
 * javadoc of the implementing classes to determine their behaviour in this
 * case.
 * 
 * @author b.elliottsmith
 */
public interface AnyMap<K, V> extends AnyReadMap<K, V> {

	/**
	 * Attempt to add the pair to the map, returning <code>false</code> if it could not be added, and <code>true</code> otherwise. In a
	 * <code>ListMap</code> the return value will always be <code>true</code>; however in a <code>MultiMap</code> it will be
	 * <code>false</code>, and hence the map remain unmodified, if a pair whose key and value are both equal to the ones provided (as
	 * determined by the map's definitions of equality) is already present; and in a <code>Map</code> it will be <code>false</code> if a
	 * pair whose key is equal to the one provided is already present. When the value of the pair is inequal to <code>null</code>, this is
	 * equivalent to <code>put(key, value) == null</code><br />
	 * 
	 * @param key
	 *            key to insert
	 * @param value
	 *            value to insert
	 * @return <code>true</code> if the map was modified, <code>false</code> otherwise
	 */
	public boolean add(K key, V value);
	
	/**
	 * Ensures that the provided key binds to the provided value at least once;
	 * depending on the underlying implementation this may remove any existing 
	 * key->value pairs where the key is equal to the one provided (<code>Map</code>),
	 * the key and value are both equal (<code>MultiMap</code>), or simply append
	 * this key to all existing maplets regardless of their equality (<code>ListMap</code>).
	 * If any pair is removed as a result of this action, the value of that pair is returned.
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the value of any maplet removed as a result of this action
	 */
	public V put(K key, V val);
	
	/**
	 * Attempts to bind the provided key to the provided value. If the key does not occur
	 * in the map then the value will be associated with it and null returned. If the key
	 * occurs in the map and is bound to a different value then a <code>Map</code> will return
	 * this value, whereas both <code>MultiMap</code> and <code>ListMap</code> will insert the
	 * new pair and return null. If the key->value pair is present in the map then all maps
	 * will return a value already associated with the key (in both <code>Map</code> and 
	 * <code>MultiMap</code> there will be precisely one such value)
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return implementation dependent
	 */
	public V putIfAbsent(K key, V val);
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#values(java.lang.Object)
	 */
	@Override public AnySet<V> values(K key);
	
	/**
	 * Removes all occurrences of the provided key from domain of the map,
	 * returning an integer representing the total number of items removed
	 * 
	 * @param key remove
	 * 
	 * @return the number removed
	 */
	public int remove(K key);
	
	/**
	 * Removes all occurrences of the provided key->value pair from the map,
	 * returning an integer representing the total number of items removed
	 * 
	 * @param key the key
	 * @param val the val
	 * 
	 * @return the number removed
	 */
	public int remove(K key, V val);
	
	/**
	 * Removes all occurrences of the provided key from domain of the map,
	 * returning the entries removed
	 * 
	 * @param key key to remove
	 * 
	 * @return entries removed
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key);
	
	/**
	 * Removes all occurrences of the provided key->value pair from the map,
	 * returning the entries removed
	 * 
	 * @param key key to remove
	 * @param val value to remove
	 * 
	 * @return entries removed
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val);
	
	/**
	 * Removes all occurrences of the provided key from domain of the map,
	 * returning the first such value removed, or null if none
	 * 
	 * @param key the key to remove
	 * 
	 * @return the value associated with the first key removed, or null if none
	 */
	public V removeAndReturnFirst(K key);
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#keys()
	 */
	@Override public AnySet<K> keys();
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#entries()
	 */
	@Override public AnySet<Entry<K, V>> entries();

	/**
	 * Clears the map, returning the number of elements removed from it. In a concurrent map, whilst there
	 * is no guarantee that the map will be empty when this method returns if it is being concurrently modified,
	 * the number will accurately convey the number of items removed.
	 * 
	 * @return the int
	 */
	public int clear();
	
	/**
	 * Clears the map, returning the entries removed from it. In a concurrent map the <code>Iterator</code> will accurately
	 * represent the elements that are removed from the map; <b>however</b> the <code>Iterator</code> must be exhausted for
	 * the clear operation to be guaranteed to have completed.
	 * 
	 * @return the iterator< entry< k, v>>
	 */
	public Iterator<Entry<K, V>> clearAndReturn();

	/**
	 * Returns a copy of the map. Note that this method may not necessarily return an object of the same
	 * class as the one it is called upon, but will return one indistinguishable from it with respect
	 * to all method calls.
	 * 
	 * @return the any map< k, v>
	 */
	public AnyMap<K, V> copy();
	
	/**
	 * Returns a map representing the inverse function of this map. This
	 * operation will typically be expensive unless the map is a BiMap in
	 * which case the action is trivial. The method may not return a map
	 * of the same type as the one it is called on (although typically this
	 * will be the case, and should happen wherever possible).
	 * 
	 * @return the inverse map/function of the one called upon
	 */
	public AnyMap<V, K> inverse();

	/**
	 * Returns a set representing the range of the map. Operations on this
	 * set will typically be expensive (O(n) where n is the size of the map).
	 * 
	 * @return the range of the map
	 */
	public AnySet<V> values();
	
}
