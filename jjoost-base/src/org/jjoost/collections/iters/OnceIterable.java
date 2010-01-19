package org.jjoost.collections.iters;

import java.util.Iterator;

/**
 * Given an <code>Iterator</code>, yields a one use <code>Iterable</code>
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class OnceIterable<E> implements Iterable<E> {

    public final Iterator<E> iterator ;
    public OnceIterable(Iterator<E> iterator) {
        this.iterator = iterator;
    }
    public Iterator<E> iterator() {
        return iterator ;
    }
    
}
