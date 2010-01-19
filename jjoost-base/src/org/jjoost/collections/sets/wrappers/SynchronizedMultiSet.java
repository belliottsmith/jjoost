package org.jjoost.collections.sets.wrappers;

import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;

public class SynchronizedMultiSet<V> extends SynchronizedArbitrarySet<V, MultiSet<V>> implements MultiSet<V> {
	
	private static final long serialVersionUID = -8766973234275059454L;
	
	public SynchronizedMultiSet(MultiSet<V> delegate) {		
		super(delegate) ;
	}
	
	@Override public synchronized MultiSet<V> copy() {
		return new SynchronizedMultiSet<V>(delegate.copy()) ;
	}

	@Override public synchronized void put(V val, int numberOfTimes) {
		delegate.put(val, numberOfTimes) ;
	}

	@Override
	public synchronized Set<V> unique() {
		return wrap(delegate.unique()) ;
	}
	
}
