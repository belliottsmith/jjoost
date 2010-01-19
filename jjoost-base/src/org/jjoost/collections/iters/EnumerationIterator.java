package org.jjoost.collections.iters;

import java.util.Enumeration;
import java.util.Iterator;

public class EnumerationIterator<E> implements Iterator<E> {

	private final Enumeration<? extends E> enumeration ;
	public EnumerationIterator(Enumeration<? extends E> enumeration) {
		this.enumeration = enumeration ;
	}
	
	public boolean hasNext() {
		return enumeration.hasMoreElements() ;
	}
	public E next() {
		return enumeration.nextElement() ;
	}
	public void remove() {
		throw new UnsupportedOperationException() ;
	}
	
}
