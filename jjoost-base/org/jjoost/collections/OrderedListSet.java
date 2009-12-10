package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder ;

public interface OrderedListSet<V> extends ListSet<V>, OrderedSet<V> {

	@Override public OrderedListSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	@Override public OrderedListSet<V> copy() ;
	@Override public OrderedListSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	@Override public OrderedListSet<V> filter(FilterPartialOrder<V> filter) ;

}
