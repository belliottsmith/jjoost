package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder ;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 * 
 * @author b.elliottsmith
 *
 * @param <V>
 */
public interface OrderedScalarSet<V> extends Set<V>, OrderedSet<V> {

	@Override public OrderedScalarSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	@Override public OrderedScalarSet<V> copy() ;
	@Override public OrderedScalarSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	@Override public OrderedScalarSet<V> filter(FilterPartialOrder<V> filter) ;

}
