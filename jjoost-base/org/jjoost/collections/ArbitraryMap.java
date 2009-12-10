package org.jjoost.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public interface ArbitraryMap<K, V> extends ArbitraryReadMap<K, V> {

	/**
	 * put the (key,value) pair into the map; behaviour depends on the type of Map 
	 * 
	 * @param key
	 * @param val
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
	
	/**
	 * ensures that the (key, value) pair is not present in the map, returning a boolean indicating if anything was actually removed.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public int remove(K key, V val) ;

	/**
	 * ensures that the key does not occur in the map, returning an integer representing the number of items actually removed. If this is a List or Multi map this operation may delete multiple items.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public int remove(K key) ;	
	
	/**
	 * ensures that the (key, value) pair is not present in the map, returning a boolean indicating if anything was actually removed.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) ;
	
	/**
	 * ensures that the key does not occur in the map, returning an integer representing the number of items actually removed. If this is a List or Multi map this operation may delete multiple items.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public Iterable<Entry<K, V>> removeAndReturn(K key) ;	
	
	/**
	 * ensures that the key does not occur in the map, returning an integer representing the number of items actually removed. If this is a List or Multi map this operation may delete multiple items.
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public V removeAndReturnFirst(K key) ;	
	
	/**
	 * clears the map, return an integer count of the number of nodes deleted. if the hash map supports concurrent modifications this number should accurately reflect the number of items removed from the map (although the map may be non-empty by the time the method returns if others are still inserting into it)
	 */
	public int clear() ;

	/**
	 * clears the map, returns and iterable of the contents that were removed; in thread safe implementations the set returned is guaranteed to be the complete set of entries that were removed
	 * this may be an expensive operation
	 */
	public Iterator<Entry<K, V>> clearAndReturn() ;

	/**
	 * shrinks the map (may be a NoOp depending on the Map type)
	 */
	public void shrink() ;
	
	/**
	 * returns an Iterable over all distinct keys in the map
	 * 
	 * @return
	 */
	public ArbitrarySet<K> keys() ;
	
	/**
	 * returns an Iterable of all (key,value) pairs in the map
	 * 
	 * @param key
	 * @return
	 */
	public ArbitrarySet<Map.Entry<K, V>> entries() ;

	public ArbitraryMap<K, V> copy() ;
	
	/**
	 * create the inverse of this map; this can be an expensive operation depending on the underlying map!
	 * 
	 * @return
	 */
	public ArbitraryMap<V, K> inverse() ;
	
}
