package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder;
import org.jjoost.util.tuples.Pair;

public interface OrderedReadSet<V> extends ArbitraryReadSet<V> {

	public V first() ;
	public V last() ;
	
	public V last(V find) ;

	public V first(FilterPartialOrder<V> filter) ;
	public V last(FilterPartialOrder<V> filter) ;

	public V floor(V find) ;
	public V ceil(V find) ;
	
	public V lesser(V find) ;
	public V greater(V find) ;
	
	/**
	 * find the values closest to the provided key if it does not exist, or the lowest and greatest value of the key if it does
 	 * the first argument is always the lesser of the two;
	 * is equivalent to a call to both ceil() and floor(), however the ordering of the results differs on if the key is present or not;
	 * if it is then it is equivalent to (floor, ceil); if it is not then it is equivalent to (ceil, floor). this is so that the first value is always less than the second
	 * @param find
	 * @return
	 */ 
	public Pair<V, V> boundaries(V find) ;

	// LAZY
	public Iterable<V> all(boolean asc) ;
	public Iterable<V> all(V value, boolean asc) ;	
	public Iterable<V> unique(boolean asc) ;
	public OrderedSet<V> filter(FilterPartialOrder<V> filter) ;

	// EAGER
	public int count(FilterPartialOrder<V> filter) ;
	public OrderedReadSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	public OrderedReadSet<V> copy() ;

}
