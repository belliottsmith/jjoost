package org.jjoost.collections.sets.wrappers;

import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.base.SynchronizedDelegator ;
import org.jjoost.util.Equality;

public class SynchronizedArbitrarySet<V, S extends AnySet<V>> extends SynchronizedDelegator implements AnySet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	protected final S delegate ;
	public SynchronizedArbitrarySet(S delegate) {		
		this.delegate = delegate;
	}
	
	@Override public synchronized Iterable<V> all(V value) {
		return wrap(delegate.all(value)) ;
	}
	@Override public synchronized Boolean apply(V v) {
		return delegate.apply(v);
	}
	@Override public synchronized int clear() {
		return delegate.clear();
	}
	@Override public synchronized Iterator<V> clearAndReturn() {
		return wrap(delegate.clearAndReturn()) ;
	}
	@Override public synchronized boolean contains(V value) {
		return delegate.contains(value);
	}
	@Override public synchronized AnySet<V> copy() {
		return new SynchronizedArbitrarySet<V, AnySet<V>>(delegate.copy()) ;
	}
	@Override public synchronized int count(V value) {
		return delegate.count(value);
	}
	@Override public synchronized V first(V value) {
		return delegate.first(value);
	}
	@Override public synchronized boolean isEmpty() {
		return delegate.isEmpty();
	}
	@Override public synchronized Iterator<V> iterator() {
		return wrap(delegate.iterator()) ;
	}
	@Override public synchronized List<V> list(V value) {
		return delegate.list(value);
	}
	@Override public synchronized boolean permitsDuplicates() {
		return delegate.permitsDuplicates();
	}
	@Override public synchronized boolean add(V val) {
		return delegate.add(val);
	}
	@Override public synchronized V put(V val) {
		return delegate.put(val);
	}
	@Override public synchronized int putAll(Iterable<V> val) {
		return delegate.putAll(val) ;
	}
	@Override public synchronized V putIfAbsent(V val) {
		return delegate.putIfAbsent(val);
	}
	@Override public synchronized int remove(V value) {
		return delegate.remove(value);
	}
	@Override public synchronized Iterable<V> removeAndReturn(V value) {
		return wrap(delegate.removeAndReturn(value)) ;
	}
	@Override public synchronized V removeAndReturnFirst(V value) {
		return delegate.removeAndReturnFirst(value);
	}
	@Override public synchronized void shrink() {
		delegate.shrink();
	}
	@Override public synchronized int totalCount() {
		return delegate.totalCount();
	}
	@Override public synchronized AnySet<V> unique() {
		return delegate.unique();
	}
	@Override public synchronized int uniqueCount() {
		return delegate.uniqueCount();
	}
	@Override
	public synchronized int remove(V value, int removeAtMost) {
		return delegate.remove(value, removeAtMost) ;
	}
	@Override
	public synchronized Iterable<V> removeAndReturn(V value, int removeAtMost) {
		return delegate.removeAndReturn(value, removeAtMost) ;
	}
	@Override
	public synchronized V removeAndReturnFirst(V value, int removeAtMost) {
		return delegate.removeAndReturnFirst(value, removeAtMost) ;
	}

	@Override
	public synchronized Equality<? super V> equality() {
		return delegate.equality() ;
	}

}
