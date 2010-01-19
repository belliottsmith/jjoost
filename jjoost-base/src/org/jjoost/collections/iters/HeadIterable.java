package org.jjoost.collections.iters;

import java.util.Iterator;


public class HeadIterable<E> implements Iterable<E> {

	public final Iterable<E> iterable ;
	public final int count ;
	
	public HeadIterable(Iterable<E> iterable, int count) {
		this.iterable = iterable ;
		this.count = count ;
	}

	public Iterator<E> iterator() {
		return new HeadIterator<E>(iterable.iterator(), count) ;
	}	

}
