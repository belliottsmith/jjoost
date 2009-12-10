package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.FilterPartialOrder;
import org.jjoost.util.tuples.Pair;

// all methods accepting total order filters ensure that the filter's accept() method is always called in strictly the specified ascending or descending order (ascending if none is specified)

public interface OrderedReadMap<K, V> extends ArbitraryReadMap<K, V> {

	public V last(K key) ;

	public Iterable<V> values(boolean asc) ;
	public Iterable<Entry<K, V>> entries(K key, boolean asc) ;
	public Iterable<V> values(K key, boolean asc) ;

	public Entry<K, V> first(FilterPartialOrder<Entry<K, V>> filter) ;
	public Entry<K, V> last(FilterPartialOrder<Entry<K, V>> filter) ;
	
	// always returns results in ascending key order
	public Iterable<Entry<K, V>> firstOfEachKey() ;
	// always returns results in descending key order
	public Iterable<Entry<K, V>> lastOfEachKey() ;

	public Entry<K, V> greaterEntry(K find) ;
	public Entry<K, V> lesserEntry(K find) ;
	public V greater(K find) ;
	public V lesser(K find) ;
	
	public Entry<K, V> floorEntry(K find) ;
	public Entry<K, V> ceilEntry(K find) ;
	public V floor(K find) ;
	public V ceil(K find) ;
	
	/**
	 * find the values closest to the provided key if it does not exist, or the lowest and greatest value of the key if it does
 	 * the first argument is always the lesser of the two;
	 * is equivalent to a call to both ceil() and floor(), however the ordering of the results differs on if the key is present or not;
	 * if it is then it is equivalent to (floor, ceil); if it is not then it is equivalent to (ceil, floor). this is so that the first value is always less than the second
	 * @param find
	 * @return
	 */ 
	public Pair<V, V> boundaries(K find) ;
	/**
	 * find the values closest to the provided key if it does not exist, or the lowest and greatest value of the key if it does
 	 * the first argument is always the lesser of the two;
	 * is equivalent to a call to both ceil() and floor(), however the ordering of the results differs on if the key is present or not;
	 * if it is then it is equivalent to (floor, ceil); if it is not then it is equivalent to (ceil, floor). this is so that the first value is always less than the second
	 * @param find
	 * @return
	 */ 
	public Pair<Entry<K, V>, Entry<K, V>> boundaryEntries(K find) ;

	// LAZY
	public OrderedReadMap<K, V> filterByKey(FilterPartialOrder<K> filter) ;
	public OrderedReadMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter) ;

	// EAGER
	public OrderedReadMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) ;
	public OrderedReadMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) ;

	// javac cannot handle the following methods; they "conflict" in OrderedMap where ArbitraryMap declares an entries() and keys() method that have differing descendents of ArbitraryReadOnlySet defined. 
	// this isn't actually a conflict as we simply need something that implements BOTH of these, which we define in OrderedMap. The Eclipse Java compiler is less flumoxed. 

	public OrderedReadSet<K> keys() ;
	public OrderedReadMapEntrySet<K, V> entries() ;
	
	public static interface OrderedReadMapEntrySet<K, V> extends OrderedReadSet<Entry<K, V>> {
		public OrderedReadMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc) ;
	}
	
	public OrderedReadMap<K, V> copy() ;

}
