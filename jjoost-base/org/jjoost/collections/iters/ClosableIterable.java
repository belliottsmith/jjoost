package org.jjoost.collections.iters;

public interface ClosableIterable<E> extends Iterable<E> {

	public ClosableIterator<E> iterator() ;
	
}
