/**
 * Copyright (c) 2010 Benedict Elliott Smith
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jjoost.collections.iters;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.jjoost.collections.Set ;
import org.jjoost.collections.sets.serial.SerialHashSet ;
import org.jjoost.util.Function;
import org.jjoost.util.Iters ;

public class TransitiveClosureIterator<E> implements Iterator<E> {
	
	private final Function<E, ? extends Iterator<E>> function ;
    private final Queue<E> results = new ArrayDeque<E>() ;
    private final Queue<E> lookup = new ArrayDeque<E>() ;
    private final Set<E> visited = new SerialHashSet<E>() ;
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
