package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.ListSet;

public class SynchronizedListSet<V> extends SynchronizedArbitrarySet<V, ListSet<V>> implements ListSet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedListSet(ListSet<V> delegate) {		
		super(delegate) ;
	}
	
	@Override public synchronized ListSet<V> copy() {
		return new SynchronizedListSet<V>(delegate.copy()) ;
	}
}
