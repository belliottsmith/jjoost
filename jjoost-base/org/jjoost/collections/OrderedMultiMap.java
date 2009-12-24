package org.jjoost.collections;

import java.util.Map ;
import java.util.Map.Entry ;

import org.jjoost.util.FilterPartialOrder ;

public interface OrderedMultiMap<K, V> extends MultiMap<K, V>, OrderedMap<K, V> {

	@Override public OrderedMultiMap<K, V> copy() ;
	@Override public OrderedMultiMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedMultiMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedMultiMap<K, V> filterByKey(FilterPartialOrder<K> filter) ;
	@Override public OrderedMultiMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter) ;
	@Override public OrderedMultiMap<K, V> removeAndReturn(FilterPartialOrder<K> filter) ;
	@Override public OrderedMultiMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;

	@Override public OrderedMultiMapEntrySet<K, V> entries() ;	
	@Override public OrderedMultiSet<K> keys() ;

	public static interface OrderedMultiMapEntrySet<K, V> extends OrderedMapEntrySet<K, V>, Set<Map.Entry<K, V>> { 
		@Override public OrderedMultiMapEntrySet<K, V> filter(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedMultiMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc) ;
		@Override public OrderedMultiMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter) ;
		@Override public OrderedMultiMapEntrySet<K, V> copy() ;
		@Override public OrderedMultiMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter) ;
	}	

}
