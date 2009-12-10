package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder;

public interface OrderedSet<V> extends ArbitrarySet<V>, OrderedReadSet<V> {

	public int remove(FilterPartialOrder<V> filter) ;
	public OrderedSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	public V removeAndReturnFirst(FilterPartialOrder<V> filter) ;
	@Override public OrderedSet<V> copy() ;
	@Override public OrderedSet<V> filterCopy(FilterPartialOrder<V> filter) ;

}
