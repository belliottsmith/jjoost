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

package org.jjoost.util.filters;

import org.jjoost.util.Filter;
import org.jjoost.util.Function;

/**
 * A filter that applies the provided function to its input before delegating to the provided filter
 * 
 * @author b.elliottsmith
 */
public class MappedFilter<X, Y> implements Filter<X> {

	private static final long serialVersionUID = -8782803136948476218L;

	private final Filter<? super Y> filter;
	private final Function<? super X, ? extends Y> mapped;

    /**
     * Constructs a filter that applies the provided function to its input before delegating to the provided filter
     * 
     * @param mapping the function to transform the input variables
     * @param filter the delegate filter
     */
	public MappedFilter(Function<? super X, ? extends Y> mapping, Filter<? super Y> filter) {
		this.filter = filter;
		this.mapped = mapping;
	}

	public boolean accept(X test) {
		return filter.accept(mapped.apply(test));
	}

    /**
     * Returns a filter that applies the provided function to its input before delegating to the provided filter
     * 
     * @param mapping the function to transform the input variables
     * @param filter the delegate filter
     * @return a filter that applies the provided function to its input before delegating to the provided filter
     */
	public static <X, Y> MappedFilter<X, Y> get(Function<? super X, ? extends Y> mapping, Filter<? super Y> filter) {
		return new MappedFilter<X, Y>(mapping, filter);
	}

}
