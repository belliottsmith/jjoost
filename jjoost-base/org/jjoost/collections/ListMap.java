package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Function;

/**
 * 
 * A ListMap supports multiple instances of the same (key,value) pairs; i.e., {1->2, 1->2} is a valid ListMap
 * 
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface ListMap<K, V> extends ArbitraryMap<K, V>, Function<K, Iterable<V>> {

	/**
	 * put the (key,value) pair into the map. always returns null.
	 * 
	 * @param key
	 * @param val
	 */
	public V put(K key, V val) ;

	/**
	 * put the (key,value) pair into the map. always returns null.
	 * 
	 * @param key
	 * @param val
	 */
	public V putIfAbsent(K key, V val) ;
	
	public ListMap<K, V> copy() ;

	@Override
	public ListSet<Entry<K, V>> entries() ;
	
	@Override
	public ListSet<K> keys() ;
	
}
