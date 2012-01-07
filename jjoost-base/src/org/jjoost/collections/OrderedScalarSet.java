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

package org.jjoost.collections;

import org.jjoost.util.FilterPartialOrder;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 * 
 * @author b.elliottsmith
 *
 * @param <V>
 */
public interface OrderedScalarSet<V> extends Set<V>, OrderedSet<V> {

	@Override public OrderedScalarSet<V> removeAndReturn(FilterPartialOrder<V> filter);
	@Override public OrderedScalarSet<V> copy();
	@Override public OrderedScalarSet<V> filterCopy(FilterPartialOrder<V> filter);
	@Override public OrderedScalarSet<V> filter(FilterPartialOrder<V> filter);

}
