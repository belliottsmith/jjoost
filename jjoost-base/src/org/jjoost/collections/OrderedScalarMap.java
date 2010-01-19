package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.FilterPartialOrder ;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 * 
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface OrderedScalarMap<K, V> extends Map<K, V>, OrderedMap<K, V> {

	@Override public OrderedScalarMap<K, V> copy() ;
	@Override public OrderedScalarMap<K, V> filterByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedScalarMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedScalarMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedScalarMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedScalarMap<K, V> removeAndReturn(FilterPartialOrder<K> filter) ;
	@Override public OrderedScalarMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	
	@Override public OrderedScalarMapEntrySet<K, V> entries() ;
	@Override public OrderedScalarSet<K> keys() ;

	public static interface OrderedScalarMapEntrySet<K, V> extends OrderedMapEntrySet<K, V>, Set<Entry<K, V>> { 
		@Override public OrderedScalarMapEntrySet<K, V> filter(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedScalarMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc) ;
		@Override public OrderedScalarMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedScalarMapEntrySet<K, V> copy() ;
		@Override public OrderedScalarMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	}	

}
