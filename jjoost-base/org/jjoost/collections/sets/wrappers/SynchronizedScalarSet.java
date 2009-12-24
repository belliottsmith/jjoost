package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.Set;

public class SynchronizedScalarSet<V> extends SynchronizedArbitrarySet<V, Set<V>> implements Set<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedScalarSet(Set<V> delegate) {
		super(delegate) ;
	}
	
	@Override public synchronized Set<V> copy() {
		return new SynchronizedScalarSet<V>(delegate.copy()) ;
	}
	@Override public synchronized V get(V key) {
		return delegate.get(key);
	}
	@Override public synchronized int size() {
		return delegate.size();
	}
}
