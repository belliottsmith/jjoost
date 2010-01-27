package org.jjoost.collections.iters;

import java.util.Iterator;

/**
 * This class creates an <code>Iterable</code> (i.e. a class with an <code>iterator()</code> method) from the supplied array 
 * 
 * @author b.elliottsmith
 *
 * @param <E> the element type
 */
public final class EmptyIterable<E> implements Iterable<E> {

	@SuppressWarnings("unchecked")
	private static final EmptyIterable INSTANCE = new EmptyIterable() ;
	
	@SuppressWarnings("unchecked")
	public static <E> EmptyIterable<E> get() {
		return INSTANCE ;
	}

	@Override
	public Iterator<E> iterator() {
		return EmptyIterator.get() ;
	}

}
