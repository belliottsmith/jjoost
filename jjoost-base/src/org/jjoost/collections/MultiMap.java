package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Function;

/**
 * This interface declares a map that permits duplicate keys, but no duplicate key->value pairs,
 * i.e. a key can map to an arbitrary set of values wherein no value occurs more than once
 */
public interface MultiMap<K, V> extends AnyMap<K, V>, Function<K, Iterable<V>> {

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
	 * @see org.jjoost.collections.AnyReadMap#values(java.lang.Object)
	 */
	@Override public Set<V> values(K key) ;

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#keys()
	 */
	@Override public MultiSet<K> keys() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#entries()
	 */
	@Override public Set<Entry<K, V>> entries() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public MultiMap<K, V> copy() ;

}
