package org.jjoost.collections.iters;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.serial.SerialScalarHashSet ;
import org.jjoost.util.Function;
import org.jjoost.util.Iters ;

public class TransitiveClosureIterator<E> implements Iterator<E> {
	
	private final Function<E, ? extends Iterator<E>> function ;
    private final Queue<E> results = new ArrayDeque<E>() ;
    private final Queue<E> lookup = new ArrayDeque<E>() ;
    private final Set<E> visited = new SerialScalarHashSet<E>() ;
    private final boolean graph ;

    public TransitiveClosureIterator(Function<E, ? extends Iterator<E>> function, E start) {
    	this(function, start, true) ;
    }
    
    public TransitiveClosureIterator(Function<E, ? extends Iterator<E>> function, E start, boolean graph) {
		this.function = function ;
		results.add(start) ;
		lookup.add(start) ;
		this.graph = graph ;
	}
    
	public boolean hasNext() {
        while (results.isEmpty() && !lookup.isEmpty()) {
        	E v = lookup.poll() ;
        	if (graph || !visited.contains(v)) { 
        		results.addAll(Iters.toList(function.apply(v))) ;
        		visited.put(v) ;
        	}
        }
        return !results.isEmpty() ;
    }
	
    public E next() {
        return results.poll() ;
    }
    
    public void remove() {
        throw new UnsupportedOperationException() ;
    }

}
