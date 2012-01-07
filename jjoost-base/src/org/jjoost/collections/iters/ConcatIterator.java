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

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class can be used to lazily concatenate together zero or more <code>Iterator</code> classes whose elements share a common super type
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class ConcatIterator<E> implements Iterator<E> {

	Iterator<? extends E> prev;
    Iterator<? extends E> next;
    final Iterator<? extends Iterator<? extends E>> nexts;

    public ConcatIterator(Iterator<? extends Iterator<? extends E>> members) {
        nexts = members;
        initNext();
    }

    public ConcatIterator(Iterator<? extends E> ... members) {
        nexts = new ArrayIterator<Iterator<? extends E>>(members);
        initNext();
    }
    
    private final void initNext() {
    	if (!nexts.hasNext()) {
    		next = Collections.<E>emptyList().iterator();
    	} else {
    		next = nexts.next();
    		moveNext();
    	}
    }
    
    private final void moveNext() {
    	while (!next.hasNext() && nexts.hasNext())
    		next = nexts.next();
    }

    public boolean hasNext() {
    	moveNext();
        return next.hasNext();
    }

    public E next() {
    	moveNext();
    	if (!next.hasNext())
    		throw new NoSuchElementException();
    	final E r = next.next();
    	prev = next;
        return r;
    }

    public void remove() {
    	if (prev == null)
    		throw new NoSuchElementException();
        prev.remove();
        prev = null;
    }
    
}
