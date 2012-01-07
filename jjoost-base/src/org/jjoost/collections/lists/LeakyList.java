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

package org.jjoost.collections.lists;

import java.util.Iterator;

import org.jjoost.collections.iters.ArrayIterator;
import org.jjoost.collections.iters.ConcatIterator;

public class LeakyList<E> implements Iterable<E> {

	private final E[] data;
	private int lb, size;
	
	@SuppressWarnings("unchecked")
	public LeakyList(int size) {
		super();
		this.data = (E[]) new Object[size];
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		if (lb + size > data.length) {
			return new ConcatIterator<E>( new ArrayIterator<E>(data, lb, data.length), new ArrayIterator<E>(data, 0, size - (data.length - lb)) );
		} else {
			return new ArrayIterator<E>(data, lb, lb + size);
		}
	}

	public void add(E value) {
		data[lb] = value;
		lb = (lb + 1) % data.length;
		if (size != data.length)
			size++;
	}
	
}
