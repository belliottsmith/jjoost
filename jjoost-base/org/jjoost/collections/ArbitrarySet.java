package org.jjoost.collections;

import java.util.Iterator;


public interface ArbitrarySet<V> extends ArbitraryReadSet<V> {

	public V put(V val) ;	
	public int putAll(Iterable<V> val) ;	
	public V putIfAbsent(V val) ;
	
	public int remove(V value) ;
	public int remove(V value, int removeAtMost) ;	
	public Iterable<V> removeAndReturn(V value) ;	
	public Iterable<V> removeAndReturn(V value, int removeAtMost) ;	
	public V removeAndReturnFirst(V value) ;	
	public V removeAndReturnFirst(V value, int removeAtMost) ;
	
	public int clear() ;	
	public Iterator<V> clearAndReturn() ;
	
	public void shrink() ;	
	public ArbitrarySet<V> copy() ;
	
}
