package org.jjoost.collections;

import java.util.Map.Entry ;

import org.jjoost.util.FilterPartialOrder;

public interface OrderedMap<K, V> extends ArbitraryMap<K, V>, OrderedReadMap<K, V> {

	public int remove(FilterPartialOrder<K> filter) ;
	public OrderedMap<K, V> removeAndReturn(FilterPartialOrder<K> filter) ;
	public OrderedMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;	
	public Entry<K, V> removeAndReturnFirst(FilterPartialOrder<K> filter) ;

	@Override public OrderedSet<K> keys() ;
	@Override public OrderedMapEntrySet<K, V> entries() ;
	
	public static interface OrderedMapEntrySet<K, V> extends OrderedReadMapEntrySet<K, V>, OrderedSet<Entry<K, V>> {
		@Override public OrderedMapEntrySet<K, V> filter(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc) ;
		@Override public OrderedMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedMapEntrySet<K, V> copy() ;
		@Override public OrderedMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	}
	
	@Override public OrderedMap<K, V> copy() ;
	@Override public OrderedMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedMap<K, V> filterByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter) ;

}
