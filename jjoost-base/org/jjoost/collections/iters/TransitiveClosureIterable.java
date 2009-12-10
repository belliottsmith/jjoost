package org.jjoost.collections.iters;

import java.util.Iterator;

import org.jjoost.util.Function;

public class TransitiveClosureIterable<E> implements Iterable<E> {
	
	private final Function<E, ? extends Iterator<E>> function ;
	private final E start ;

    public TransitiveClosureIterable(Function<E, ? extends Iterator<E>> function, E start) {
		this.function = function ;
		this.start = start ;
	}

	public Iterator<E> iterator() {
		return new TransitiveClosureIterator<E>(function, start) ;
	}

}
