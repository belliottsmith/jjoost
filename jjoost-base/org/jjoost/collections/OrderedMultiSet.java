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
public interface OrderedMultiSet<V> extends MultiSet<V>, OrderedSet<V> {

	@Override public OrderedMultiSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	@Override public OrderedMultiSet<V> copy() ;
	@Override public OrderedMultiSet<V> filterCopy(FilterPartialOrder<V> filter) ;
	@Override public OrderedMultiSet<V> filter(FilterPartialOrder<V> filter) ;

}
