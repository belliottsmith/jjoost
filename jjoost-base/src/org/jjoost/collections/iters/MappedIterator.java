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

import org.jjoost.util.Function;

/**
 * Given an <code>Iterator</code> object and a function, yields an <code>Iterator</code> 
 * representing the result of applying that function to every element of the supplied <code>Iterator</code> 
 * 
 * @author b.elliottsmith
 *
 * @param <X>
 * @param <Y>
 */
public class MappedIterator<X, Y> implements Iterator<Y> {

    final Iterator<? extends X> base;
    private final Function<? super X, ? extends Y> function;

    public MappedIterator(Iterator<? extends X> base, Function<? super X, ? extends Y> function) {
        this.base = base;
        this.function = function;
    }

    public boolean hasNext() {
        return base.hasNext();
    }

    public Y next() {
        return function.apply(base.next());
    }

    public void remove() {
        base.remove();
    }
    
}
