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

import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.util.FilterPartialOrder;

/**
 * The Ordered* interfaces are not finalised, nor are any implementing classes yet provided. 
 * Javadoc will be provided once they are settled and made available.
 *   
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface OrderedListMap<K, V> extends ListMap<K, V>, OrderedMap<K, V> {

	@Override public OrderedListMap<K, V> copy();
	@Override public OrderedListMap<K, V> filterCopyByKey(FilterPartialOrder<K> filter);
	@Override public OrderedListMap<K, V> filterCopyByEntry(FilterPartialOrder<Entry<K, V>> filter);
	@Override public OrderedListMap<K, V> filterByKey(FilterPartialOrder<K> filter);
	@Override public OrderedListMap<K, V> filterByEntry(FilterPartialOrder<Entry<K, V>> filter);
	@Override public OrderedListMap<K, V> removeAndReturn(FilterPartialOrder<K> filter);
	@Override public OrderedListMap<K, V> removeByEntryAndReturn(FilterPartialOrder<Entry<K, V>> filter);
	@Override public OrderedListMapEntrySet<K, V> entries();
	@Override public OrderedMultiSet<K> keys();

	public static interface OrderedListMapEntrySet<K, V> extends OrderedMapEntrySet<K, V>, MultiSet<Map.Entry<K, V>> {
		@Override public OrderedListMapEntrySet<K, V> filter(FilterPartialOrder<Entry<K, V>> filter);
		@Override public OrderedListMapEntrySet<K, V> filterByKey(FilterPartialOrder<K> filter, boolean asc);
		@Override public OrderedListMapEntrySet<K, V> filterCopy(FilterPartialOrder<Entry<K, V>> filter);
		@Override public OrderedListMapEntrySet<K, V> copy();
		@Override public OrderedListMapEntrySet<K, V> removeAndReturn(FilterPartialOrder<Entry<K, V>> filter);
	}	

}
