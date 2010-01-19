package org.jjoost.collections.iters;

import java.util.Iterator;


public class DropIterable<E> implements Iterable<E> {

	public final Iterable<E> iterable ;
	public final int count ;
	
	public DropIterable(Iterable<E> iterable, int count) {
		this.iterable = iterable ;
		this.count = count ;
	}

	public Iterator<E> iterator() {
		final Iterator<E> iter = iterable.iterator() ;
		int dropped = 0 ;
		while (iter.hasNext() && dropped < count) {
			dropped++ ;
			iter.next() ;
		}
		return iter ;
	}	

}
