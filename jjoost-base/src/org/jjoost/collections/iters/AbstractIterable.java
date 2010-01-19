package org.jjoost.collections.iters;

import org.jjoost.util.Iters ;

public abstract class AbstractIterable<E> implements Iterable<E> {

	public String toString() {
		return Iters.toString(this) ;
	}
	
}
