package org.jjoost.collections.iters;

import java.util.Iterator;

public class DestructiveIterator<E> implements Iterator<E> {
	
	private final Iterator<E> delegate ;
	public DestructiveIterator(Iterator<E> delegate) {
		super();
		this.delegate = delegate;
	}

	public boolean hasNext() {
		return delegate.hasNext();
	}

	public E next() {
		final E next = delegate.next();
		delegate.remove() ;
		return next ;
	}

	public void remove() {
	}

}
