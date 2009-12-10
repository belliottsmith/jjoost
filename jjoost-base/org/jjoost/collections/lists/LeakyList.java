package org.jjoost.collections.lists;

import java.util.Iterator;

import org.jjoost.collections.iters.ArrayIterator ;
import org.jjoost.collections.iters.ConcatIterator ;

public class LeakyList<E> implements Iterable<E> {

	private final E[] data ;
	private int lb, size ;
	
	@SuppressWarnings("unchecked")
	public LeakyList(int size) {
		super() ;
		this.data = (E[]) new Object[size] ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		if (lb + size > data.length) {
			return new ConcatIterator( new ArrayIterator<E>(data, lb, data.length), new ArrayIterator<E>(data, 0, size - (data.length - lb)) ) ;
		} else {
			return new ArrayIterator<E>(data, lb, lb + size) ;
		}
	}

	public void add(E value) {
		data[lb] = value ;
		lb = (lb + 1) % data.length ;
		if (size != data.length)
			size++ ;
	}
	
}
