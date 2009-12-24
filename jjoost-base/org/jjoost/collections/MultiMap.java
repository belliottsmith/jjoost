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
public interface MultiMap<K, V> extends AnyMap<K, V>, Function<K, Iterable<V>> {

	@Override public V put(K key, V val) ;
	@Override public V putIfAbsent(K key, V val) ;

	@Override public Set<V> values(K key) ;

	@Override public MultiSet<K> keys() ;
	@Override public Set<Entry<K, V>> entries() ;
	
	@Override public MultiMap<K, V> copy() ;

}
