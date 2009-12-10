package org.jjoost.collections;

import java.util.Iterator;


public interface ArbitrarySet<V> extends ArbitraryReadSet<V> {

	/**
	 * put the provided value into the set, returning the value previously present (or null if none)
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V put(V val) ;
	
	/**
	 * put the provided values into the set, returning an integer representing the number actually inserted
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public int putAll(Iterable<V> val) ;
	
	/**
	 * put the provided value into the set if it does not already exist; if it does exist, the first such value is returned and no values are added
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V putIfAbsent(V val) ;
	
	/**
	 * ensures that the value is not present in the map, returning an integer representing the number of items removed
	 * 
	 * @param value
	 */
	public int remove(V value) ;

	public Iterable<V> removeAndReturn(V value) ;
	
	public V removeAndReturnFirst(V value) ;
	
	/**
	 * clears the map
	 */
	public int clear() ;
	
	/**
	 * clears the map, returns and iterable of the contents that were removed; in thread safe implementations the set returned is guaranteed to be the complete set of entries that were removed
	 */
	public Iterator<V> clearAndReturn() ;

	public void shrink() ;
	
	public ArbitrarySet<V> copy() ;
	
}
