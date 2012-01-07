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

import org.jjoost.util.Filter;

/**
 * Lazily filters the underlying iterator by the supplied filter.
 * 
 * @author b.elliottsmith
 *
 * @param <E>
 */
public class FilteredIterable<E> implements Iterable<E> {

    private Iterable<? extends E> wrapped;
    private Filter<? super E> filter;

    public FilteredIterable(Iterable<? extends E> wrapped, Filter<? super E> filter) {
        this.wrapped = wrapped;
        this.filter = filter;
    }

    public Iterator<E> iterator() {
        return new FilteredIterator<E>(wrapped.iterator(), filter);
    }

}
