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

import org.jjoost.util.Filter;

public class FilteredClosableIterator<E> extends FilteredIterator<E> implements ClosableIterator<E> {

    public FilteredClosableIterator(ClosableIterator<? extends E> base, Filter<? super E> filter) {
    	super(base, filter);
    }

	@SuppressWarnings("unchecked")
	public void close() {
		((ClosableIterator<E>) base).close();
	}

	public static <E> FilteredClosableIterator<E> get(ClosableIterator<? extends E> base, Filter<? super E> filter) {
		return new FilteredClosableIterator<E>(base, filter);
	}
	
}
