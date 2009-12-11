package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.Factory;
import org.jjoost.util.Function;

/**
 * A ScalarMap permits at most one value for each possible key, much like the java.util.HashMap
 * 
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface ScalarMap<K, V> extends ArbitraryMap<K, V>, Function<K, V> {

	/**
	 * put the provided (key, value) pair into the map, returning the value previously associated with the key 
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V put(K key, V val) ;
	
	/**
	 * put the provided (key, value) pair into the map IFF the key does not already map to a value; returns the existing value or null if none.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putIfAbsent(K key, V val) ;
	
	/**
	 * put the provided (key, value) pair into the map IFF there is no such (key, value) pair already; return true if we added something
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	
	/**
	 * retrieve the value associated with the key, or if there is no such value then produce one from the provided factory and add it to the map before returning it.
	 * the factory method is guaranteed NOT to be called if there is an existing record with the provided key. 
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) ;
	
	/**
	 * retrieve the value associated with the key, or if there is no such value then produce one from the provided factory and add it to the map before returning it.
	 * the factory method is guaranteed NOT to be called if there is an existing record with the provided key. 
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) ;
	public V get(K key) ;


	/**
	 * 	convenience method; for ScalarMap <code>totalCount() == uniqueKeyCount() == size()</code>
	 * 
	 * @return
	 */
	public int size() ;
	
	@Override public ScalarMap<K, V> copy() ;
	@Override public ScalarSet<Entry<K, V>> entries() ;
	@Override public ScalarSet<K> keys() ;
	
}
