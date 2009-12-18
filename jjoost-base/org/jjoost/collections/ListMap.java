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

	@Override public V put(K key, V val) ;
	@Override public V putIfAbsent(K key, V val) ;
	
	@Override public MultiSet<V> values(K key) ;

	@Override public MultiSet<K> keys() ;
	@Override public MultiSet<Entry<K, V>> entries() ;
	
	@Override public ListMap<K, V> copy() ;

}
