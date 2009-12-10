package org.jjoost.collections.iters;

import org.jjoost.util.Filter;

public class FilteredClosableIterator<E> extends FilteredIterator<E> implements ClosableIterator<E> {

    public FilteredClosableIterator(ClosableIterator<? extends E> base, Filter<? super E> filter) {
    	super(base, filter) ;
    }

	@SuppressWarnings("unchecked")
	public void close() {
		((ClosableIterator<E>) base).close() ;
	}

	public static <E> FilteredClosableIterator<E> get(ClosableIterator<? extends E> base, Filter<? super E> filter) {
		return new FilteredClosableIterator<E>(base, filter) ;
	}
	
}
