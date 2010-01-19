package org.jjoost.collections.maps.wrappers;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.base.SynchronizedDelegator ;

public abstract class SynchronizedArbitraryMap<K, V, M extends AnyMap<K, V>> extends SynchronizedDelegator implements AnyMap<K, V> {
	
	private static final long serialVersionUID = -7183655836427941893L ;
	
	final M delegate ;
	
	public SynchronizedArbitraryMap(M delegate) {
		this.delegate = delegate;
	}
	
	@Override public synchronized int clear() {
		return delegate.clear();
	}
	@Override public synchronized Iterator<Entry<K, V>> clearAndReturn() {
		return wrap(delegate.clearAndReturn()) ;
	}
	@Override public synchronized boolean contains(K key, V val) {
		return delegate.contains(key, val);
	}
	@Override public synchronized boolean contains(K key) {
		return delegate.contains(key);
	}
	@Override public synchronized int count(K key, V val) {
		return delegate.count(key, val);
	}
	@Override public synchronized int count(K key) {
		return delegate.count(key);
	}
	@Override public synchronized Iterable<Entry<K, V>> entries(K key) {
		return wrap(delegate.entries(key)) ;
	}
	@Override public synchronized V first(K key) {
		return delegate.first(key);
	}
	@Override public synchronized AnyMap<V, K> inverse() {
		return delegate.inverse();
	}
	@Override public synchronized boolean isEmpty() {
		return delegate.isEmpty();
	}
	@Override public synchronized List<V> list(K key) {
		return delegate.list(key);
	}
	@Override public synchronized boolean permitsDuplicateKeys() {
		return delegate.permitsDuplicateKeys();
	}
	@Override public synchronized boolean add(K key, V val) {
		return delegate.add(key, val);
	}
	@Override public synchronized V put(K key, V val) {
		return delegate.put(key, val);
	}
	@Override public synchronized V putIfAbsent(K key, V val) {
		return delegate.putIfAbsent(key, val);
	}
	@Override public synchronized int remove(K key, V val) {
		return delegate.remove(key, val);
	}
	@Override public synchronized int remove(K key) {
		return delegate.remove(key);
	}
	@Override public synchronized Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return wrap(delegate.removeAndReturn(key, val)) ;
	}
	@Override public synchronized Iterable<Entry<K, V>> removeAndReturn(K key) {
		return wrap(delegate.removeAndReturn(key)) ;
	}
	@Override public synchronized V removeAndReturnFirst(K key) {
		return delegate.removeAndReturnFirst(key);
	}
	@Override public synchronized void shrink() {
		delegate.shrink();
	}
	@Override public synchronized int totalCount() {
		return delegate.totalCount();
	}
	@Override public synchronized int uniqueKeyCount() {
		return delegate.uniqueKeyCount();
	}
	@Override public synchronized AnySet<V> values() {
		return wrap(delegate.values()) ;
	}

}
