package org.jjoost.collections.iters;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class UniformIterator<E> implements Iterator<E> {

	final E repeat ;
	private int count ;
	
	public UniformIterator(E repeat, int count) {
		this.repeat = repeat ;
		this.count = count ;
	}
	
	public boolean hasNext() {
		return count != 0 ;
	}

	public E next() {
		if (count == 0)
			throw new NoSuchElementException() ;
		count-- ;
		return repeat ;
	}

	public void remove() {
		throw new UnsupportedOperationException() ;
	}

	/**
	 * returns the number of items we have returned so far for this iterator; useful to determine how many items were actually used from the underlying iterator.
	 * @return
	 */
	public int count() {
		return count ;
	}
	
}
