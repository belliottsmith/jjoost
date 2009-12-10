package org.jjoost.collections.iters;

public class HeadClosableIterator<E> extends HeadIterator<E> implements ClosableIterator<E> {

	public HeadClosableIterator(ClosableIterator<E> iterator, int count) {
		super(iterator, count) ;
	}
	
	@Override
	public boolean hasNext() {
		final boolean r = super.hasNext() ;
		if (!r) close() ;
		return r ;
	}

	public void close() {
		((ClosableIterator<E>) iterator).close() ;
	}
	
}
