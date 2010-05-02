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

import java.util.Iterator;
import java.util.Map.Entry;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.ListMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.collections.sets.base.AbstractUniqueSetAdapter;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;

public class NestedSetListMap<K, V> extends NestedSetMap<K, V, MultiSet<V>> implements ListMap<K, V> {

	private static final long serialVersionUID = -490119082143181821L;

	public NestedSetListMap(Map<K, MultiSet<V>> map,
			Equality<? super V> valueEq, Factory<MultiSet<V>> factory) {
		super(map, valueEq, factory);
	}

	protected MultiSet<Entry<K, V>> entrySet ;	
	@Override
	public MultiSet<Entry<K, V>> entries() {
		if (entrySet == null) {
			entrySet = new EntrySet() ;
		}
		return entrySet ;
	}

	@Override
	public Iterable<V> apply(K v) {
		return values(v) ;
	}

	@Override
	public ListMap<K, V> copy() {
		final Map<K, MultiSet<V>> copy = map.copy() ;
		for (Entry<K, MultiSet<V>> entry : copy.entries())
			entry.setValue(entry.getValue().copy()) ;
		return new NestedSetListMap<K, V>(copy, valueEq, factory) ;
	}
	
	protected final class EntrySet extends AbstractEntrySet implements MultiSet<Entry<K, V>> {

		private static final long serialVersionUID = 8122351713234623044L;
		private UniqueEntrySet unique ;
		
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return null ;
		}
		
		@Override
		public boolean add(Entry<K, V> entry) {
			NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return true ;
		}

				@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final V r = NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
			return r == null ? null : new ImmutableMapEntry<K, V>(entry.getKey(), r) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return true ;
		}

		@Override
		public MultiSet<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public void put(Entry<K, V> entry, int numberOfTimes) {
			for (int i = 0 ; i != numberOfTimes ; i++)
				NestedSetListMap.this.put(entry.getKey(), entry.getValue()) ;
		}
		
		public Set<Entry<K, V>> unique() {
			if (unique == null)
				unique = new UniqueEntrySet() ;
			return unique ;
		}

		private final class UniqueEntrySet extends AbstractUniqueSetAdapter<Entry<K, V>> {
			private static final long serialVersionUID = 686867617922872433L;
			@Override
			protected AnySet<Entry<K, V>> set() {
				return EntrySet.this ;
			}
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return Iters.concat(Functions.apply(new UniqueMultiEntryMaker<K, V>(), map.entries())).iterator() ;
			}
		}
		
	}
	
	private static final class UniqueMultiEntryMaker<K, V> implements Function<Entry<K, ? extends AnySet<V>>, Iterable<Entry<K, V>>> {
		private static final long serialVersionUID = -965724235732791909L;
		private final UpdateableEntryMaker<K, V> f = new UpdateableEntryMaker<K, V>() ;
		@Override
		public Iterable<Entry<K, V>> apply(Entry<K, ? extends AnySet<V>> entry) {
			f.update(entry.getKey()) ;
			return Functions.apply(f, entry.getValue().unique()) ;
		}

	}
}
