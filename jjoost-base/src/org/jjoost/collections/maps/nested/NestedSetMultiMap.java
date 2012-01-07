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

package org.jjoost.collections.maps.nested;

import java.util.Map.Entry;

import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;

public class NestedSetMultiMap<K, V> extends NestedSetMap<K, V, Set<V>> implements MultiMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public NestedSetMultiMap(Map<K, Set<V>> map, Equality<? super V> valueEq,
			Factory<Set<V>> factory) {
		super(map, valueEq, factory);
	}

	protected Set<Entry<K, V>> entrySet;
	@Override
	public Set<Entry<K, V>> entries() {
		if (entrySet == null) {
			entrySet = new EntrySet();
		}
		return entrySet;
	}
	@Override
	public MultiMap<K, V> copy() {
		final Map<K, Set<V>> copy = map.copy();
		for (Entry<K, Set<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy());
		return new NestedSetMultiMap<K, V>(copy, valueEq, factory);
	}
	
	@Override
	public Iterable<V> apply(K v) {
		return values(v);
	}
	
	protected final class EntrySet extends AbstractEntrySet implements Set<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;

		@Override
		public boolean add(Entry<K, V> entry) {
			return NestedSetMultiMap.this.add(entry.getKey(), entry.getValue());
		}
		
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {			
			NestedSetMultiMap.this.put(entry.getKey(), entry.getValue());
			return get(entry);
		}

		@Override
		public Set<Entry<K, V>> unique() {
			return this;
		}
		
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = NestedSetMultiMap.this.put(entry.getKey(), entry.getValue());
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r);
		}
		
		@Override
		public boolean permitsDuplicates() {
			return false;
		}

		@Override
		public Entry<K, V> get(Entry<K, V> key) {
			return first(key);
		}
		@Override
		public int size() {
			return totalCount();
		}
		public Set<Entry<K, V>> copy() {
			throw new UnsupportedOperationException();
		}
		
	}

}
