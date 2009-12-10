package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface ArbitraryReadMap<K, V> extends Serializable {

	/**
	 * returns a boolean indicating if the supplied key occurs in the map
	 * 
	 * @param key
	 * @return
	 */
	public boolean contains(K key) ;
	
	/**
	 * returns an int indicating the number of occurrences of the supplied key occurs in the map
	 * 
	 * @param key
	 * @return
	 */
	public int count(K key) ;
	
	/**
	 * returns a boolean indicating if the supplied (key, value) pair occurs in the map
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public boolean contains(K key, V val) ;

	/**
	 * returns an int indicating the number of occurrences of the supplied key and value pair occurs in the map
	 * 
	 * @param key
	 * @return
	 */
	public int count(K key, V val) ;
	
	/**
	 * returns the first matching value for the provided key; which for a scalar map is the same as a get() for the STLs
	 * 
	 * @param key
	 * @return
	 */
	public V first(K key) ;
	
	/**
	 * returns a lazy Iterable over all values the provided key maps to
	 * 
	 * @param key
	 * @return
	 */
	public Iterable<V> values(K key) ;

	/**
	 * returns a list of all values the provided key maps to (this is an eager operation)
	 * 
	 * @param key
	 * @return
	 */
	public List<V> list(K key) ;
	
	/**
	 * returns an Iterable over all values in the map
	 * 
	 * @return
	 */
	public Iterable<V> values() ;
	
	/**
	 * returns an Iterable over all distinct keys in the map
	 * 
	 * @return
	 */
	public ArbitraryReadSet<K> keys() ;
	
	/**
	 * returns an Iterable of all (key,value) pairs in the map
	 * 
	 * @param key
	 * @return
	 */
	public ArbitraryReadSet<Map.Entry<K, V>> entries() ;
	
	/**
	 * returns an Iterable of all (key,value) pairs mapped by the provided key
	 * 
	 * @param key
	 * @return
	 */
	public Iterable<Map.Entry<K, V>> entries(K key) ;
	
	public boolean isEmpty() ;	
	public int totalCount() ;	
	public int uniqueKeyCount() ;

	public boolean permitsDuplicateKeys() ;

}
