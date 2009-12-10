package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder ;

public interface OrderedScalarSet<V> extends ScalarSet<V>, OrderedSet<V> {

	@Override public OrderedScalarSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	@Override public OrderedScalarSet<V> copy() ;
	@Override public OrderedScalarSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	@Override public OrderedScalarSet<V> filter(FilterPartialOrder<V> filter) ;

}
