package org.jjoost.collections.iters;

import java.util.Iterator;

public class ClosableIteratorWrapper<E> implements ClosableIterator<E> {

	private final Iterator<? extends E> wrapped ;
	public ClosableIteratorWrapper(Iterator<? extends E> wrapped) {
		this.wrapped = wrapped ;
	}

	public void close() { }
	
	public boolean hasNext() {
		return wrapped.hasNext() ;
	}

	public E next() {
		return wrapped.next() ;
	}

	public void remove() {
		wrapped.remove() ;
	}
	
	public static <E> ClosableIteratorWrapper<E> get(Iterator<? extends E> towrap) {
		return new ClosableIteratorWrapper<E>(towrap) ;
	}
	
}
