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
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.collections.sets.base.AbstractSet;
import org.jjoost.collections.sets.base.IterableSet;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;

public abstract class NestedSetMap<K, V, S extends AnySet<V>> implements AnyMap<K, V> {

	private static final long serialVersionUID = -6962291049889502542L;
	
	protected final Map<K, S> map;
	protected final Factory<S> factory;
	protected final Equality<? super V> valueEq;
	private volatile int totalCount;
	
	@SuppressWarnings("rawtypes")
	private static final AtomicIntegerFieldUpdater<NestedSetMap> totalCountUpdater = AtomicIntegerFieldUpdater.newUpdater(NestedSetMap.class, "totalCount");
	
	protected MultiSet<K> keySet;
	@Override
	public MultiSet<K> keys() {
		if (keySet == null) {
			keySet = new KeySet();
		}
		return keySet ;
	}

	public NestedSetMap(Map<K, S> map, Equality<? super V> valueEq, Factory<S> factory) {
		super();
		this.map = map;
		this.factory = factory;
		this.valueEq = valueEq;
	}

	@Override
	public boolean contains(K key, V value) {
		final S set = map.get(key);
		return set != null && set.contains(value);
	}
	@Override
	public boolean contains(K key) {
		return map.contains(key);
	}
	@Override
	public int count(K key, V value) {
		final S set = map.get(key);
		return set == null ? 0 : set.count(value);
	}
	@Override
	public int count(K key) {
		final S set = map.get(key);
		return set == null ? 0 : set.totalCount();
	}
	@Override
	public Iterable<Entry<K, V>> entries(K key) {
		final S set = map.get(key);
		if (set == null) 
			return Iters.emptyIterable();
		return Functions.apply(new EntryMaker<K, V>(key), set);
	}
	@Override
	public V first(K key) {
		final S set = map.get(key);
		return set == null ? null : set.isEmpty() ? null : set.iterator().next();
	}
	@Override
	public List<V> list(K key) {
		final S set = map.get(key);
		return set == null ? null : Iters.toList(set);
	}
	@Override
	public int totalCount() {
		return totalCount;
	}
	@Override
	public int uniqueKeyCount() {
		return map.totalCount();
	}
	@Override
	public AnySet<V> values() {
		return new ValueSet();
	}
	@Override
	public S values(K key) {
		final S set = map.get(key);
		return set == null ? null : set;
	}
	@Override
	public boolean add(K key, V val) {
		if (map.ensureAndGet(key, factory).add(val)) {
			totalCountUpdater.incrementAndGet(this);
			return true;
		}
		return false;
	}
	@Override
	public V put(K key, V val) {
		final S set = map.ensureAndGet(key, factory);
		if (set.contains(val))
			return set.put(val);
		totalCountUpdater.incrementAndGet(this);
		return set.put(val);
	}
	@Override
	public V putIfAbsent(K key, V val) {
		final S set = map.ensureAndGet(key, factory);
		if (set.contains(val))
			return set.put(val);
		return set.first(val);
	}
	
	@Override
	public boolean permitsDuplicateKeys() {
		return true;
	}

	@Override
	public int clear() {
		int c = 0;
		Iterator<Entry<K, S>> iter = map.entries().iterator();
		while (iter.hasNext()) {
			c += iter.next().getValue().clear();
		}
		return c;
	}
	
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		throw new UnsupportedOperationException();
	}

	@Override
	public AnyMap<V, K> inverse() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int remove(K key, V value) {
		final S set = map.get(key);
		if (set == null)
			return 0;
		final int r = set.remove(value);
		if (r != 0)
			totalCountUpdater.addAndGet(this, -r);
		if (set.isEmpty())
			map.remove(key);
		return r;
	}
	@Override
	public int remove(K key) {
		S removed = map.removeAndReturnFirst(key);
		final int r = removed.totalCount();
		if (r != 0)
			totalCountUpdater.addAndGet(this, -r);
		return r;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V value) {
		final S set = map.get(key);
		if (set == null)
			return Iters.emptyIterable();
		final Iterable<V> r = set.removeAndReturn(value);
		{	final Iterator<?> iter = r.iterator();
			if (iter.hasNext())
				totalCountUpdater.addAndGet(this, -Iters.count(iter));
		}
		return Functions.apply(new EntryMaker<K, V>(key), r);
	}
	
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {		
		S removed = map.removeAndReturnFirst(key);
		if (!removed.isEmpty())
			totalCountUpdater.addAndGet(this, -removed.totalCount());
		return Functions.apply(new EntryMaker<K, V>(key), removed);
	}

	@Override
	public V removeAndReturnFirst(K key) {
		S removed = map.removeAndReturnFirst(key);
		if (removed == null)
			return null;
		final Iterator<V> vals = removed.iterator();
		if (vals.hasNext()) {
			final V r = vals.next();
			final int deleted = removed.clear();
			totalCountUpdater.addAndGet(this, -deleted);
			return r;
		}
		return null;
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public void shrink() {
		map.shrink();
	}

	class ValueSet extends IterableSet<V> {
		private static final long serialVersionUID = 7253995600614012301L;
		@Override
		public Equality<? super V> equality() {
			return valueEq;
		}
		@Override
		public Iterator<V> iterator() {
			return Iters.concat(Functions.apply(Functions.<S, Entry<K, S>>getMapEntryValueProjection(), map.entries())).iterator();
		}
	}
	
	class KeySet extends AbstractSet<K> implements MultiSet<K> {
		
		private static final long serialVersionUID = 1461826147890179114L;

		@Override
		public void put(K val, int numberOfTimes) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean contains(K value) {
			return NestedSetMap.this.contains(value);
		}

		@Override
		public int count(K value) {
			return NestedSetMap.this.count(value);
		}

		@Override
		public int totalCount() {
			return NestedSetMap.this.totalCount();
		}

		@Override
		public int clear() {
			return NestedSetMap.this.clear();
		}

		@Override
		public void shrink() {
			NestedSetMap.this.shrink();
		}
		
		@Override
		public Boolean apply(K v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE;
		}

		@Override
		public MultiSet<K> copy() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterable<K> all(final K key) {
			final S set = map.get(key);
			return new UniformList<K>(key, set == null ? 0 : set.totalCount());
		}
		
		@Override
		public K first(final K key) {
			return map.keys().first(key);
		}
		
		@Override
		public List<K> list(final K key) {
			final S set = map.get(key);
			return new UniformList<K>(key, set == null ? 0 : set.totalCount());
		}
		
		@Override
		public Iterator<K> iterator() {
			return Iters.concat(Functions.apply(NestedSetMap.<K, V>keyRepeater(), map.entries())).iterator();
		}

		@Override
		public boolean isEmpty() {
			return NestedSetMap.this.isEmpty();
		}

		@Override
		public int uniqueCount() {
			return NestedSetMap.this.uniqueKeyCount();
		}

		@Override
		public final boolean add(K val) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public final K put(K val) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<K> unique() {
			return map.keys().unique();
		}

		@Override
		public K putIfAbsent(K val) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int putAll(Iterable<K> val) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<K> clearAndReturn() {
			return Functions.apply(Functions.<K, Entry<K, V>>getMapEntryKeyProjection(), NestedSetMap.this.clearAndReturn());
		}
		
		@Override
		public boolean permitsDuplicates() {
			return true;
		}

		@Override
		public int remove(K key) {
			return NestedSetMap.this.remove(key);
		}

		@Override
		public Iterable<K> removeAndReturn(K key) {
			return Functions.apply(Functions.<K, Entry<K, V>>getMapEntryKeyProjection(), NestedSetMap.this.removeAndReturn(key));
		}

		@Override
		public K removeAndReturnFirst(K key) {
			final Iterator<? extends Entry<K, S>> removed = map.removeAndReturn(key).iterator();
			return removed.hasNext() ? removed.next().getKey() : null;
		}

		@Override
		public int remove(K key, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove fewer than zero elements");
				return 0;
			}
			final S set = map.first(key);
			final Iterator<?> iter = set.iterator();
			int removed = 0;
			while ((removed != removeAtMost) && iter.hasNext()) {
				iter.next();
				iter.remove();
			}
			if (removed != 0)
				totalCountUpdater.addAndGet(NestedSetMap.this, -removed);
			if (set.isEmpty())
				map.remove(key);
			return removed;
		}

		@Override
		public Iterable<K> removeAndReturn(K key, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove fewer than zero elements");
				return Iters.emptyIterable();
			}
			final S set = map.first(key);
			final Iterator<?> iter = set.iterator();
			int removed = 0;
			while ((removed != removeAtMost) && iter.hasNext()) {
				iter.next();
				iter.remove();
			}
			if (removed != 0)
				totalCountUpdater.addAndGet(NestedSetMap.this, -removed);
			if (set.isEmpty())
				map.remove(key);
			return new UniformList<K>(key, removed);
		}

		@Override
		public K removeAndReturnFirst(K key, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove fewer than zero elements");
				return null;
			}
			final S set = map.first(key);
			final Iterator<?> iter = set.iterator();
			int removed = 0;
			while ((removed != removeAtMost) && iter.hasNext()) {
				iter.next();
				iter.remove();
			}
			if (removed != 0)
				totalCountUpdater.addAndGet(NestedSetMap.this, -removed);
			if (set.isEmpty())
				map.remove(key);
			return key;
		}

		@Override
		public Equality<? super K> equality() {
			return map.keys().equality();
		}
		
	}
	
	abstract class AbstractEntrySet extends AbstractSet<Entry<K, V>> implements AnySet<Entry<K, V>> {
		
		private static final long serialVersionUID = 4037233101289518536L;

		@Override
		public boolean contains(Entry<K, V> value) {
			return NestedSetMap.this.contains(value.getKey(), value.getValue());
		}

		@Override
		public int count(Entry<K, V> value) {
			return NestedSetMap.this.count(value.getKey(), value.getValue());
		}

		@Override
		public int totalCount() {
			return NestedSetMap.this.totalCount();
		}

		@Override
		public int clear() {
			return NestedSetMap.this.clear();
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return NestedSetMap.this.clearAndReturn();
		}
		
		@Override
		public void shrink() {
			NestedSetMap.this.shrink();
		}
		
		@Override
		public int remove(Entry<K, V> entry) {
			return NestedSetMap.this.remove(entry.getKey(), entry.getValue());
		}

		@Override
		public Boolean apply(Entry<K, V> v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE;
		}
		
		@Override
		public Iterable<Entry<K, V>> all(Entry<K, V> entry) {
			final K key = entry.getKey();
			final V value = entry.getValue();
			final S set = map.get(key);
			if (set == null) 
				return Iters.emptyIterable();
			return Functions.apply(new EntryMaker<K, V>(key), set.all(value));
		}

		@Override
		public Entry<K, V> first(Entry<K, V> entry) {
			final K key = entry.getKey();
			final V value = entry.getValue();
			final S set = map.get(key);
			if (set == null)
				return null;
			final V first = set.first(value);
			return first == null ? null : new ImmutableMapEntry<K, V>(key, first);
		}

		@Override
		public List<Entry<K, V>> list(Entry<K, V> entry) {
			return Iters.toList(all(entry));
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return Iters.concat(Functions.apply(new MultiEntryMaker<K, V>(), map.entries())).iterator();
		}
		
		@Override
		public boolean isEmpty() {
			return NestedSetMap.this.isEmpty();
		}
		
		@Override
		public int uniqueCount() {
			return NestedSetMap.this.totalCount();
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry) {
			return NestedSetMap.this.removeAndReturn(entry.getKey(), entry.getValue());
		}

		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry) {
			final Iterator<? extends Entry<K, V>> removed = NestedSetMap.this.removeAndReturn(entry.getKey(), entry.getValue()).iterator();
			return removed.hasNext() ? removed.next() : null;
		}

		@Override
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0;
			for (Entry<K, V> val : vals)
				if (put(val) == null)
					c++;
			return c;
		}

		@Override
		public int remove(Entry<K, V> entry, int removeAtMost) {
			final S set = map.get(entry.getKey());
			if (set == null)
				return 0;
			final int r = set.remove(entry.getValue(), removeAtMost);
			if (r != 0) {
				totalCountUpdater.addAndGet(NestedSetMap.this, -r);
				if (set.isEmpty())
					map.remove(entry.getKey());
			}
			return r;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry, int removeAtMost) {
			final S set = map.get(entry.getKey());
			if (set == null)
				return Iters.emptyIterable();
			final Iterable<V> iter = set.removeAndReturn(entry.getValue(), removeAtMost);
			final int r = Iters.count(iter);
			if (r != 0) {
				totalCountUpdater.addAndGet(NestedSetMap.this, -r);
				if (set.isEmpty())
					map.remove(entry.getKey());
				return Functions.apply(new EntryMaker<K, V>(entry.getKey()), iter);
			}
			return Iters.emptyIterable();
		}

		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry, int removeAtMost) {
			final S set = map.get(entry.getKey());
			if (set == null)
				return null;
			final Iterable<V> iter = set.removeAndReturn(entry.getValue(), removeAtMost);
			final int r = Iters.count(iter);
			if (r != 0) {
				totalCountUpdater.addAndGet(NestedSetMap.this, -r);
				if (set.isEmpty())
					map.remove(entry.getKey());
				return new ImmutableMapEntry<K, V>(entry.getKey(), iter.iterator().next());
			}
			return null;
		}

		@Override
		public Equality<? super Entry<K, V>> equality() {
			return Equalities.forMapEntries(map.keys().equality(), valueEq);
		}

	}

	private static final class EntryMaker<K, V> implements Function<V, Entry<K, V>> {
		private static final long serialVersionUID = -965724235732791909L;
		private final K key;
		public EntryMaker(K key) {
			this.key = key;
		}
		@Override
		public Entry<K, V> apply(V value) {
			return new ImmutableMapEntry<K, V>(key, value);
		}
	}

	protected static final class UpdateableEntryMaker<K, V> implements Function<V, Entry<K, V>> {
		private static final long serialVersionUID = -965724235732791909L;
		private K key;
		public void update(K key) { this.key = key ; }
		@Override
		public Entry<K, V> apply(V value) {
			return new ImmutableMapEntry<K, V>(key, value);
		}
	}
	
	private static final class MultiEntryMaker<K, V> implements Function<Entry<K, ? extends Iterable<V>>, Iterable<Entry<K, V>>> {
		private static final long serialVersionUID = -965724235732791909L;
		private final UpdateableEntryMaker<K, V> f = new UpdateableEntryMaker<K, V>();
		@Override
		public Iterable<Entry<K, V>> apply(Entry<K, ? extends Iterable<V>> entry) {
			f.update(entry.getKey());
			return Functions.apply(f, entry.getValue());
		}

	}
	
	@SuppressWarnings("rawtypes")
	private static final KeyRepeater KEY_REPEATER = new KeyRepeater();
	@SuppressWarnings("unchecked")
	private static final <K, V> KeyRepeater<K, V> keyRepeater() {
		return KEY_REPEATER;
	}
	private static final class KeyRepeater<K, V> implements Function<Entry<K, ? extends AnySet<V>>, Iterable<K>> {
		private static final long serialVersionUID = -965724235732791909L;
		@Override
		public Iterable<K> apply(Entry<K, ? extends AnySet<V>> entry) {
			return new UniformList<K>(entry.getKey(), entry.getValue().totalCount());
		}
	}

}
