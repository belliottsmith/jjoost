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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.util.Function;
import org.jjoost.util.Functions;

/**
 * This class can be used to lazily concatenate together zero or more <code>Iterable</code> classes whose elements share a common super type
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class ConcatIterable<E> implements Iterable<E> {

    private final List<Iterable<? extends E>> members;
    public ConcatIterable(final Iterator<? extends Iterable<? extends E>> members) {
    	this.members = new ArrayList<Iterable<? extends E>>();
    	while (members.hasNext())
    		this.members.add(members.next());
    }
    public ConcatIterable(final Iterable<? extends Iterable<? extends E>> members) {
        this.members = new ArrayList<Iterable<? extends E>>();
        for (Iterable<? extends E> member : members) 
        	this.members.add(member);
    }
    public ConcatIterable(@SuppressWarnings("unchecked") final Iterable<? extends E> ... members) {
        this.members = new ArrayList<Iterable<? extends E>>();
        for (Iterable<? extends E> member : members) 
        	this.members.add(member);
    }
    
    public Iterator<E> iterator() { return new ConcatIterator<E>(Functions.apply(ConcatIterable.<E>iteratorProjection(), members.iterator())) ; }
    
    @SuppressWarnings("unchecked")
	private static final <E> IteratorProjection<E> iteratorProjection() {
    	return ITERATOR_PROJECTION;
    }
    @SuppressWarnings("rawtypes")
	private static final IteratorProjection ITERATOR_PROJECTION = new IteratorProjection();
    private static final class IteratorProjection<E> implements Function<Iterable<? extends E>, Iterator<? extends E>> {
		private static final long serialVersionUID = 5360954401522205920L;

		@Override
		public Iterator<? extends E> apply(Iterable<? extends E> v) {
			return v.iterator();
		}
    }

    public void add(Iterable<? extends E> iter) { this.members.add(iter) ; }

}
