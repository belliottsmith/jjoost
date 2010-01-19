package org.jjoost.collections.iters;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class creates an <code>Iterable</code> (i.e. a class with an <code>iterator()</code> method) from the supplied array 
 * 
 * @author b.elliottsmith
 *
 * @param <E> the element type
 */
public final class EmptyIterator<E> implements Iterator<E> {

	@SuppressWarnings("unchecked")
	private static final EmptyIterator INSTANCE = new EmptyIterator() ;
	@SuppressWarnings("unchecked")
	public static <E> EmptyIterator<E> get() {
		return INSTANCE ;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public E next() {
		throw new NoSuchElementException() ;
	}

	@Override
	public void remove() {
		throw new NoSuchElementException() ;
	}
	
}
