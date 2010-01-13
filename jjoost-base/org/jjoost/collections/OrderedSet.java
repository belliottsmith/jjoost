package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 * 
 * @author b.elliottsmith
 *
 * @param <V>
 */
public interface OrderedSet<V> extends AnySet<V>, OrderedReadSet<V> {

	public int remove(FilterPartialOrder<V> filter) ;
	public OrderedSet<V> removeAndReturn(FilterPartialOrder<V> filter) ;
	public V removeAndReturnFirst(FilterPartialOrder<V> filter) ;
	@Override public OrderedSet<V> copy() ;
	@Override public OrderedSet<V> filterCopy(FilterPartialOrder<V> filter) ;

}
