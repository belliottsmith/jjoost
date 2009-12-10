package org.jjoost.collections;

import java.util.Map ;
import java.util.Map.Entry ;

import org.jjoost.util.FilterPartialOrder ;

public interface OrderedListMap<K, V> extends ListMap<K, V>, OrderedMap<K, V> {

	@Override public OrderedListMap<K, V> copy() ;
	@Override public OrderedListMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedListMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedListMap<K, V> filterByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedListMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedListMap<K, V> removeAndReturn(FilterPartialOrder<K> filter) ;
	@Override public OrderedListMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedListMapEntrySet<K, V> entries() ;
	@Override public OrderedListSet<K> keys() ;

	public static interface OrderedListMapEntrySet<K, V> extends OrderedMapEntrySet<K, V>, ListSet<Map.Entry<K, V>> {
		@Override public OrderedListMapEntrySet<K, V> filter(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedListMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc) ;
		@Override public OrderedListMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedListMapEntrySet<K, V> copy() ;
		@Override public OrderedListMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	}	

}
