package org.jjoost.collections.iters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.util.Function ;
import org.jjoost.util.Functions ;

/**
 * This class can be used to lazily concatenate together zero or more <code>Iterable</code> classes whose elements share a common super type
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class ConcatIterable<E> implements Iterable<E> {

//    public static final class ConcatIterableIterator<E> implements Iterator<E> {
//        Iterator<? extends E> current ;
//        final Iterator<? extends Iterable<? extends E>> next;
//        public ConcatIterableIterator(Iterator<? extends Iterable<? extends E>> members) {
//            next = members ;
//        }
//        public boolean hasNext() {
//            while ((current == null || !current.hasNext()) && next.hasNext())
//                current = next.next().iterator() ;
//            return current != null && current.hasNext() ;
//        }
//        public E next() {
//            while ((current == null || !current.hasNext()) && next.hasNext())
//                current = next.next().iterator() ;
//            return current.next() ;
//        }
//        public void remove() { throw new UnsupportedOperationException() ; }
//    }

    private final List<Iterable<? extends E>> members ;
    public ConcatIterable(final Iterator<? extends Iterable<? extends E>> members) {
    	this.members = new ArrayList<Iterable<? extends E>>() ;
    	while (members.hasNext())
    		this.members.add(members.next()) ;
    }
    public ConcatIterable(final Iterable<? extends Iterable<? extends E>> members) {
        this.members = new ArrayList<Iterable<? extends E>>() ;
        for (Iterable<? extends E> member : members) 
        	this.members.add(member) ;
    }
    public ConcatIterable(final Iterable<? extends E> ... members) {
        this.members = new ArrayList<Iterable<? extends E>>() ;
        for (Iterable<? extends E> member : members) 
        	this.members.add(member) ;
    }
    
    public Iterator<E> iterator() { return new ConcatIterator<E>(Functions.apply(ConcatIterable.<E>iteratorProjection(), members.iterator())) ; }
    
    @SuppressWarnings("unchecked")
	private static final <E> IteratorProjection<E> iteratorProjection() {
    	return ITERATOR_PROJECTION ;
    }
    @SuppressWarnings("unchecked")
	private static final IteratorProjection ITERATOR_PROJECTION = new IteratorProjection() ;
    private static final class IteratorProjection<E> implements Function<Iterable<? extends E>, Iterator<? extends E>> {
		private static final long serialVersionUID = 5360954401522205920L ;

		@Override
		public Iterator<? extends E> apply(Iterable<? extends E> v) {
			return v.iterator() ;
		}
    }

    public void add(Iterable<? extends E> iter) { this.members.add(iter) ; }

}
