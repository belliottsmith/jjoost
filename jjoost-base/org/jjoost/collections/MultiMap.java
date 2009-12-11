package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Function;

/**
 * 
 * A MultiMap supports a single key mapping to multiple values, i.e. {1->2, 1->3} is a valid MultiMap
 * 
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface MultiMap<K, V> extends ArbitraryMap<K, V>, Function<K, Iterable<V>> {

	/**
	 * put the provided (key, value) pair into the map, returning the value previously associated with the pair. Note this will leave untouched any pairs where the key is the same but the value is different. 
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V put(K key, V val) ;

	/**
	 * put the provided (key, value) pair into the map IFF there is no such (key, value) pair already; return the existing value or null if none.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putIfAbsent(K key, V val) ;

	public MultiMap<K, V> copy() ;
	
	@Override
	public ScalarSet<Entry<K, V>> entries() ;
	
	@Override
	public ListSet<K> keys() ;
	
}
