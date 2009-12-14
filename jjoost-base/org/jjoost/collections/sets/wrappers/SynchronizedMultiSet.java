package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.MultiSet;

public class SynchronizedMultiSet<V> extends SynchronizedArbitrarySet<V, MultiSet<V>> implements MultiSet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedMultiSet(MultiSet<V> delegate) {		
		super(delegate) ;
	}
	
	@Override public synchronized MultiSet<V> copy() {
		return new SynchronizedMultiSet<V>(delegate.copy()) ;
	}
}
