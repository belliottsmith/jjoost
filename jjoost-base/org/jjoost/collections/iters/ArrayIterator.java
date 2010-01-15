package org.jjoost.collections.iters;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class creates an <code>Iterator</code> from the supplied array
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public final class ArrayIterator<E> implements Iterator<E> {

    private final E[] vals ;
    private final int ub ;
    int p ;

    @SuppressWarnings("unchecked")
	public ArrayIterator(E ... vals) {
        this.vals = vals == null ? (E[]) new Object[0] : vals ;
        p = 0 ;
        ub = this.vals.length ;
    }

	public ArrayIterator(E[] vals, int lb, int ub) {
		if (lb > ub)
			throw new IllegalArgumentException(String.format("The provided lower bound (%d) is greater than the provided upper bound (%d)", lb, ub)) ;
		if (ub > vals.length)
			throw new IllegalArgumentException(String.format("The provided upper bound (%d) is greater than the length of the array (%d)", ub, vals.length)) ;
		if (lb < 0)
			throw new IllegalArgumentException(String.format("The provided lower bound (%d) is less than zero", lb)) ;
        this.vals = vals ;
        p = lb ;
        this.ub = ub ; 
    }

    public boolean hasNext() {
        return p != ub ;
    }

    public E next() {
    	if (p == vals.length)
    		throw new NoSuchElementException() ;
        return vals[p++] ;
    }

    public void remove() {
        throw new UnsupportedOperationException() ;
    }

    public static <E> ArrayIterator<E> get(E ... vals) {
    	return new ArrayIterator<E>(vals) ;
    }
    
}
