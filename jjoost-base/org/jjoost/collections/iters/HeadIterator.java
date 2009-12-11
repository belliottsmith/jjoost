package org.jjoost.collections.iters;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class HeadIterator<E> implements Iterator<E> {

	final Iterator<E> iterator ;
	private int take ;
	private int count ;
	
	public HeadIterator(Iterator<E> iterator, int take) {
		this.iterator = iterator ;
		this.take = take ;
	}
	
	public boolean hasNext() {
		return count != take && iterator.hasNext() ;
	}

	public E next() {
		if (count == take)
			throw new NoSuchElementException() ;
		count++ ;
		E next = iterator.next() ;
		return next ;
	}

	public void remove() {
		iterator.remove() ;
	}

	/**
	 * returns the number of items we have returned so far for this iterator; useful to determine how many items were actually used from the underlying iterator.
	 * @return
	 */
	public int count() {
		return count ;
	}
	
}
