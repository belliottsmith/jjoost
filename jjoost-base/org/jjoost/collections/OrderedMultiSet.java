package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder ;

public interface OrderedMultiSet<V> extends MultiSet<V>, OrderedSet<V> {

	@Override public OrderedMultiSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	@Override public OrderedMultiSet<V> copy() ;
	@Override public OrderedMultiSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	@Override public OrderedMultiSet<V> filter(FilterPartialOrder<V> filter) ;

}
