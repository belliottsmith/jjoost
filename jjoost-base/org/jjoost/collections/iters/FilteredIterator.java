package org.jjoost.collections.iters;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jjoost.util.Filter;

public class FilteredIterator<E> implements Iterator<E> {

    final Iterator<? extends E> base ;
    private final Filter<? super E> filter ;
    private E nextElement ;
    private Boolean hasNextElement ;

    public FilteredIterator(Iterator<? extends E> base, Filter<? super E> filter) {
        this.base = base ;
        this.filter = filter ;
        next() ;
    }

    public boolean hasNext() {
        if (hasNextElement == null) {
        	boolean hasNextElement = false ;
        	E nextElement = null ;
            while (base.hasNext() && !(hasNextElement = filter.accept(nextElement = base.next()))) ;
            this.hasNextElement = hasNextElement ;
            this.nextElement = nextElement ;
        }
        return hasNextElement == Boolean.TRUE ;
    }

    public E next() {
    	if (!hasNext())
    		throw new NoSuchElementException() ;
        hasNextElement = null ;
        return nextElement ;
    }

    public void remove() {
        base.remove() ;
    }

}
