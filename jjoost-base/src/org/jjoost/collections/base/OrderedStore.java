package org.jjoost.collections.base;

import java.util.Comparator ;
import java.util.Iterator ;

import org.jjoost.util.FilterPartialOrder ;
import org.jjoost.util.Function ;
import org.jjoost.util.tuples.Pair ;

public interface OrderedStore<N, S extends OrderedStore<N, S>> {

    public int count() ;
	public boolean isEmpty() ;
	
	public int clear() ;	
	public <V> Iterator<V> clearAndReturn(Function<? super N, ? extends V> f) ;
	public <V> Iterator<V> iterator(boolean asc, Function<? super N, V> ret) ;
	
	public <C> S copy(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	
	public boolean removeExistingNode(N n) ;

	// *************************************
	// INSERTION METHODS
	// *************************************
	
	public <V> V put(N put, Function<? super N, ? extends V> ret) ;
	public <C, V> V put(C find, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, Function<? super C, N> factory, Function<? super N, ? extends V> ret) ;
	public <V> V putIfAbsent(N put, Function<? super N, ? extends V> ret) ;
	public <C, V> V putIfAbsent(C put, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, Function<? super C, N> factory, Function<? super N, ? extends V> ret) ;
	public <C, V> V ensureAndGet(C put, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, Function<? super C, N> factory, Function<? super N, ? extends V> ret) ;

	// *************************************
	// METHODS ON ITEMS WITHOUT FILTERS
	// *************************************
	
	public <C> boolean contains(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C> int count(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C, V> V first(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V last(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> Iterator<V> all(boolean asc, C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;

	public <C, F, V> Pair<V, V> boundaries(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V ceil(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V floor(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V lesser(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V greater(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;

	public <C> int remove(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C, V> Iterable<V> removeAndReturn(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V removeAndReturnFirst(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;

	// *************************************
	// METHODS ON VALUES WITH FILTERS
	// *************************************
	
	public <C, F, V> Pair<V, V> boundaries(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends F> filterF, Comparator<? super F> filterCmp, boolean filterCmpIsTotalOrder, FilterPartialOrder<F> filter, Function<? super N, ? extends V> ret) ;
	public <C, F, V> V ceil(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends F> filterF, Comparator<? super F> filterCmp, boolean filterCmpIsTotalOrder, FilterPartialOrder<F> filter, Function<? super N, ? extends V> ret) ;
	public <C, F, V> V floor(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends F> filterF, Comparator<? super F> filterCmp, boolean filterCmpIsTotalOrder, FilterPartialOrder<F> filter, Function<? super N, ? extends V> ret) ;
	public <C, F, V> V lesser(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends F> filterF, Comparator<? super F> filterCmp, boolean filterCmpIsTotalOrder, FilterPartialOrder<F> filter, Function<? super N, ? extends V> ret) ;
	public <C, F, V> V greater(C c, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends F> filterF, Comparator<? super F> filterCmp, boolean filterCmpIsTotalOrder, FilterPartialOrder<F> filter, Function<? super N, ? extends V> ret) ;
	
	// *************************************
	// METHODS ON FILTERS
	// *************************************
	
	public <C> boolean contains(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C> int count(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C, V> V first(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> V last(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	public <C, V> Iterator<V> all(boolean asc, FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;


	public <C> int remove(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C> S removeAndReturn(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder) ;
	public <C, V> V removeAndReturnFirst(FilterPartialOrder<C> filter, Function<? super N, ? extends C> cmpF, Comparator<? super C> cmp, boolean cmpIsTotalOrder, Function<? super N, ? extends V> ret) ;
	
}
