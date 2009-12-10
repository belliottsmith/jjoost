package org.jjoost.collections.iters;

import java.util.Iterator;

import org.jjoost.util.Iters ;
import org.jjoost.util.Objects;

public abstract class AbstractIterable<E> implements Iterable<E> {

	public String toString() {
		return Iters.toString(this) ;
	}
	
	@SuppressWarnings("unchecked")
	public boolean equals(Object that) {
		return that instanceof Iterable && equals((Iterable<?>)that) ;
	}
	
	public boolean equals(Iterable<?> that) {
		if (this == that)
			return true ;
		final Iterator<?> a = this.iterator() ;
		final Iterator<?> b = this.iterator() ;
		while (a.hasNext() && b.hasNext()) {
			if (!Objects.equalQuick(a.next(), b.next()))
				return false ;
		}
		return a.hasNext() == b.hasNext() ;
	}
	
}
