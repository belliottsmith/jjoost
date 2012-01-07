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

/**
 * This class creates an <code>Iterable</code> (i.e. a class with an <code>iterator()</code> method) from the supplied array 
 * 
 * @author b.elliottsmith
 *
 * @param <E> the element type
 */
public final class EmptyIterable<E> implements Iterable<E> {

	@SuppressWarnings("unchecked")
	private static final EmptyIterable INSTANCE = new EmptyIterable();
	
	@SuppressWarnings("unchecked")
	public static <E> EmptyIterable<E> get() {
		return INSTANCE;
	}

	@Override
	public Iterator<E> iterator() {
		return EmptyIterator.get();
	}

}
