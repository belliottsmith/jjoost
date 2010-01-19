package org.jjoost.collections.iters;

import java.util.Iterator;

import org.jjoost.util.Filter;

/**
 * Lazily filters the underlying iterator by the supplied filter.
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class FilteredIterable<E> implements Iterable<E> {

    private Iterable<? extends E> wrapped ;
    private Filter<? super E> filter ;

    public FilteredIterable(Iterable<? extends E> wrapped, Filter<? super E> filter) {
        this.wrapped = wrapped;
        this.filter = filter;
    }

    public Iterator<E> iterator() {
        return new FilteredIterator<E>(wrapped.iterator(), filter) ;
    }

}
