package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Function;

/**
 * This interface declares a map that permits duplicate keys <b>and</b> duplicate key->value pairs,
 * i.e. a key can map to an arbitrary combination of possibly duplicated values
 */
public interface ListMap<K, V> extends AnyMap<K, V>, Function<K, Iterable<V>> {

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
	 * @see org.jjoost.collections.AnyReadMap#values(java.lang.Object)
	 */
	@Override public MultiSet<V> values(K key) ;

	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#keys()
	 */
	@Override public MultiSet<K> keys() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyReadMap#entries()
	 */
	@Override public MultiSet<Entry<K, V>> entries() ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.AnyMap#copy()
	 */
	@Override public ListMap<K, V> copy() ;

}
