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
