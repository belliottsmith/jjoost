package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.ScalarSet;

public class SynchronizedScalarSet<V> extends SynchronizedArbitrarySet<V, ScalarSet<V>> implements ScalarSet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedScalarSet(ScalarSet<V> delegate) {
		super(delegate) ;
	}
	
	@Override public synchronized ScalarSet<V> copy() {
		return new SynchronizedScalarSet<V>(delegate.copy()) ;
	}
	@Override public synchronized V get(V key) {
		return delegate.get(key);
	}
	@Override public synchronized int size() {
		return delegate.size();
	}
}
