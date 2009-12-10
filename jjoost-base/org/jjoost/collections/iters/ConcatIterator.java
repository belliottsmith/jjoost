package org.jjoost.collections.iters;

import java.util.Collections ;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class can be used to lazily concatenate together zero or more <code>Iterator</code> classes whose elements share a common super type
 * 
 * @author Benedict Elliott Smith
 *
 * @param <E>
 */
public class ConcatIterator<E> implements Iterator<E> {

	Iterator<? extends E> prev ;
    Iterator<? extends E> next ;
    final Iterator<? extends Iterator<? extends E>> nexts ;

    public ConcatIterator(Iterator<? extends Iterator<? extends E>> members) {
        nexts = members ;
        initNext() ;
    }

    public ConcatIterator(Iterator<? extends E> ... members) {
        nexts = new ArrayIterator<Iterator<? extends E>>(members) ;
        initNext() ;
    }
    
    private final void initNext() {
    	if (!nexts.hasNext()) {
    		next = Collections.<E>emptyList().iterator() ;
    	} else {
    		next = nexts.next() ;
    		moveNext() ;
    	}
    }
    
    private final void moveNext() {
    	while (!next.hasNext() && nexts.hasNext())
    		next = nexts.next() ;
    }

    public boolean hasNext() {
        return next.hasNext() ;
    }

    public E next() {
    	if (next == null)
    		throw new NoSuchElementException() ;
    	final E r = next.next() ;
    	prev = next ;
    	moveNext() ;
        return r ;
    }

    public void remove() {
    	if (prev == null)
    		throw new NoSuchElementException() ;
        prev.remove() ;
        prev = null ;
    }
    
}
