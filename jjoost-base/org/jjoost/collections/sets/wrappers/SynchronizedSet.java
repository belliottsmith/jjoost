package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.Set;

public class SynchronizedSet<V> extends SynchronizedArbitrarySet<V, Set<V>> implements Set<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedSet(Set<V> delegate) {
		super(delegate) ;
	}
	
	@Override public synchronized Set<V> unique() {
		return delegate.unique();
	}
	@Override public synchronized Set<V> copy() {
		return new SynchronizedSet<V>(delegate.copy()) ;
	}
	@Override public synchronized V get(V key) {
		return delegate.get(key);
	}
	@Override public synchronized int size() {
		return delegate.size();
	}
}
