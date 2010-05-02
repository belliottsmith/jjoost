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

public class HeadIterator<E> implements Iterator<E> {

	final Iterator<E> iterator ;
	private int take ;
	private int count ;
	
	public HeadIterator(Iterator<E> iterator, int take) {
		if (take < 0)
			throw new IllegalArgumentException(String.format("HeadIterator cannot limit itself to fewer than zero items (%d requested)", take)) ;
		this.iterator = iterator ;
		this.take = take ;
	}
	
	public boolean hasNext() {
		return count != take && iterator.hasNext() ;
	}

	public E next() {
		if (count == take)
			throw new NoSuchElementException() ;
		count++ ;
		E next = iterator.next() ;
		return next ;
	}

	public void remove() {
		iterator.remove() ;
	}

	/**
	 * returns the number of items we have returned so far for this iterator; useful to determine how many items were actually used from the underlying iterator.
	 * @return
	 */
	public int count() {
		return count ;
	}
	
}
