package org.jjoost.collections.base;

import java.util.Iterator;

public abstract class SynchronizedDelegator {

	protected <V> Iterable<V> wrap(final Iterable<V> iter) {
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				synchronized (SynchronizedDelegator.this) {
					return wrap(iter.iterator()) ;
				}
			}
		} ;
	}
	
	protected <V> Iterator<V> wrap(final Iterator<V> iter) {
		return new Iterator<V>() {
			@Override
			public boolean hasNext() {
				synchronized (SynchronizedDelegator.this) {
					return iter.hasNext() ;
				}
			}
			@Override
			public V next() {
				synchronized (SynchronizedDelegator.this) {
					return iter.next() ;
				}
			}

			@Override
			public void remove() {
				synchronized (SynchronizedDelegator.this) {
					iter.remove() ;
				}
			}
		} ;
	}

}
