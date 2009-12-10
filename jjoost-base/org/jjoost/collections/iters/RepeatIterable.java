package org.jjoost.collections.iters;

import java.util.Iterator;


public class RepeatIterable<E> implements Iterable<E> {

	public final E repeat ;
	public final int count ;
	
	public RepeatIterable(E repeat, int count) {
		this.repeat = repeat ;
		this.count = count ;
	}

	public Iterator<E> iterator() {
		return new RepeatIterator<E>(repeat, count) ;
	}	

}
