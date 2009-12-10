package org.jjoost.collections.iters;

import java.util.Iterator;

import org.jjoost.util.Filter;

public class FilteredIterator<E> implements Iterator<E> {

    final Iterator<? extends E> base ;
    private final Filter<? super E> filter ;
    private E nextElement ;
    private boolean hasNextElement ;

    public FilteredIterator(Iterator<? extends E> base, Filter<? super E> filter) {
        this.base = base ;
        this.filter = filter ;
        next() ;
    }

    public boolean hasNext() {
        return hasNextElement ;
    }

    public E next() {
        hasNextElement = false ;
        E r = nextElement ;
        while (base.hasNext() && !(hasNextElement = filter.accept(nextElement = base.next()))) { }
        return r ;
    }

    public void remove() {
        base.remove() ;
    }

}
