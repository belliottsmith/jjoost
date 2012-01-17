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

package org.jjoost.collections.base;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnyReadMap;
import org.jjoost.collections.AnyReadSet;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.ListMap;
import org.jjoost.collections.Map;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.ReadMap;
import org.jjoost.collections.ReadSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitaryReadSet;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class SynchronizedDelegator {

	protected final class SyncIterator<V> implements Iterator<V> {

		final Iterator<V> iter;
		public SyncIterator(Iterator<V> iter) {
			this.iter = iter;
		}
		
		@Override
		public boolean hasNext() {
			synchronized (SynchronizedDelegator.this) {
				return iter.hasNext();
			}
		}
		@Override
		public V next() {
			synchronized (SynchronizedDelegator.this) {
				return iter.next();
			}
		}

		@Override
		public void remove() {
			synchronized (SynchronizedDelegator.this) {
				iter.remove();
			}
		}

	}
	
	protected class SyncIterable<V, I extends Iterable<V>> implements Iterable<V> {
		
		final I delegate;
		public SyncIterable(I delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public final Iterator<V> iterator() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.iterator());
			}
		}
		
	}
	
	protected class SyncAnyReadSet<V, I extends AnyReadSet<V>> extends SyncIterable<V, I> implements AnyReadSet<V> {
		
		private static final long serialVersionUID = -6659656215877484794L;
		
		public SyncAnyReadSet(I set) {
			super(set);
		}
		
		@Override public Iterable<V> all(V value) {
			synchronized(SynchronizedDelegator.this) {
				return wrap(delegate.all(value));
			}
		}
		@Override public Boolean apply(V v) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.apply(v);
			}
		}
		@Override public boolean contains(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.contains(value);
			}
		}
		@Override public AnyReadSet<V> copy() {
			synchronized(SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public int count(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.count(value);
			}
		}
		@Override public V first(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.first(value);
			}
		}
		@Override public boolean isEmpty() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.isEmpty();
			}
		}
		@Override public List<V> list(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.list(value);
			}
		}
		@Override public boolean permitsDuplicates() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.permitsDuplicates();
			}
		}
		@Override public int totalCount() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.totalCount();
			}
		}
		@Override public int uniqueCount() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.uniqueCount();
			}
		}
		@Override public Equality<? super V> equality() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.equality();
			}
		}
		@Override
		public ReadMap<V, Integer> asMap() {
			synchronized(SynchronizedDelegator.this) {
				return wrap(delegate.asMap());
			}
		}
		@Override
		public ReadSet<V> unique() {
			synchronized(SynchronizedDelegator.this) {
				return wrap(delegate.unique());
			}
		}
		
	}
	
	protected class SyncReadSet<V, I extends ReadSet<V>> extends SyncAnyReadSet<V, I> implements ReadSet<V> {
		
		private static final long serialVersionUID = -6659656215877484794L;
		
		public SyncReadSet(I set) {
			super(set);
		}
		
		@Override public ReadSet<V> copy() {
			synchronized(SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public V get(V find) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.get(find);
			}
		}

		@Override
		public int size() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.size();
			}
		}
		
	}
	
	protected class SyncUnitaryReadSet<V, I extends UnitaryReadSet<V>> extends SyncReadSet<V, I> implements UnitaryReadSet<V> {
		
		private static final long serialVersionUID = -6659656215877484794L;
		
		public SyncUnitaryReadSet(I set) {
			super(set);
		}
		
		@Override public UnitaryReadSet<V> copy() {
			synchronized(SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public V get() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.get();
			}
		}
		@Override
		public UnitaryReadSet<V> unique() {
			synchronized(SynchronizedDelegator.this) {
				final UnitaryReadSet<V> r = delegate.unique();
				return r == delegate ? this : wrap(r);
			}
		}
		
	}
	
	protected class SyncAnySet<V, I extends AnySet<V>> extends SyncAnyReadSet<V, I> implements AnySet<V> {
		
		private static final long serialVersionUID = -6659656215877484794L;

		public SyncAnySet(I set) {
			super(set);
		}
		
		@Override public int clear() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.clear();
			}
		}
		@Override public Iterator<V> clearAndReturn() {
			synchronized(SynchronizedDelegator.this) {
				return delegate.clearAndReturn();
			}
		}
		@Override public AnySet<V> copy() {
			synchronized(SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public V put(V val) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.put(val);
			}
		}
		@Override public int putAll(Iterable<V> val) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.putAll(val);
			}
		}
		@Override public V putIfAbsent(V val) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.putIfAbsent(val);
			}
		}
		@Override public int remove(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.remove(value);
			}
		}
		@Override public Iterable<V> removeAndReturn(V value) {
			synchronized(SynchronizedDelegator.this) {
				return wrap(delegate.removeAndReturn(value));
			}
		}
		@Override public V removeAndReturnFirst(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.removeAndReturnFirst(value);
			}
		}
		@Override public Set<V> unique() {
			synchronized(SynchronizedDelegator.this) {
				return wrap(delegate.unique());
			}
		}
		@Override public int remove(V value, int removeAtMost) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.remove(value, removeAtMost);
			}
		}
		@Override public Iterable<V> removeAndReturn(V value, int removeAtMost) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.removeAndReturn(value, removeAtMost);
			}
		}
		@Override public V removeAndReturnFirst(V value, int removeAtMost) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.removeAndReturnFirst(value, removeAtMost);
			}
		}
		@Override public boolean add(V value) {
			synchronized(SynchronizedDelegator.this) {
				return delegate.add(value);
			}
		}
		@Override public void retain(AnySet<? super V> remove) {
			synchronized(SynchronizedDelegator.this) {
				delegate.retain(remove);
			}
		}

		
	}
	
	protected class SyncSet<V, I extends Set<V>> extends SyncAnySet<V, I> implements Set<V> {
		
		private static final long serialVersionUID = -3056587011817527947L;

		public SyncSet(I set) {
			super(set);
		}

		@Override
		public V get(V find) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.get(find);
			}
		}

		@Override
		public int size() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.size();
			}
		}
		
		@Override
		public Set<V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		
	}
	
	protected class SyncMultiSet<V, I extends MultiSet<V>> extends SyncAnySet<V, I> implements MultiSet<V> {
		
		private static final long serialVersionUID = -5785923569245766291L;

		public SyncMultiSet(I set) {
			super(set);
		}
		
		@Override
		public void put(V val, int numberOfTimes) {
			synchronized (SynchronizedDelegator.this) {
				delegate.put(val, numberOfTimes);
			}			
		}

		@Override
		public MultiSet<V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		
	}
	
	protected class SyncUnitarySet<V, I extends UnitarySet<V>> extends SyncSet<V, I> implements UnitarySet<V> {
		
		private static final long serialVersionUID = 2060537396163292489L;

		public SyncUnitarySet(I set) {
			super(set);
		}

		@Override
		public V get() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.get();
			}
		}

		@Override
		public V replace(V value) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.replace(value);
			}
		}

		@Override
		public V putOrReplace(V value) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.putOrReplace(value);
			}
		}

		@Override
		public UnitarySet<V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}

		@Override
		public UnitarySet<V> unique() {
			synchronized (SynchronizedDelegator.this) {
				final UnitarySet<V> r = delegate.unique();
				return r == delegate ? this : wrap(r);
			}
		}
		
	}
	
	protected class SyncAnyReadMap<K, V, M extends AnyReadMap<K, V>> implements AnyReadMap<K, V> {
		
		private static final long serialVersionUID = 1L;
		final M delegate;
		public SyncAnyReadMap(M delegate) {
			this.delegate = delegate;
		}
		
		@Override public boolean contains(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.contains(key);
			}
		}
		@Override public boolean contains(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.contains(key, val);
			}
		}
		@Override public int count(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.count(key);
			}
		}
		@Override public int count(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.count(key, val);
			}
		}
		@Override public V first(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.first(key);
			}
		}
		@Override public List<V> list(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.list(key);
			}
		}
		@Override public AnyReadMap<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public AnyReadSet<V> values() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values());
			}
		}
		@Override public Iterable<Entry<K, V>> entries(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries(key));
			}
		}
		@Override public int totalCount() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.totalCount();
			}
		}
		@Override public int uniqueKeyCount() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.uniqueKeyCount();
			}
		}
		@Override public boolean isEmpty() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.isEmpty();
			}
		}
		@Override public boolean permitsDuplicateKeys() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.permitsDuplicateKeys();
			}
		}
		@Override public AnyReadSet<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		} @Override
		public AnyReadSet<? extends Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}
		@Override public AnyReadSet<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}		
		
	}
	

	protected class SyncReadMap<K, V, M extends ReadMap<K, V>> extends SyncAnyReadMap<K, V, ReadMap<K, V>> implements ReadMap<K, V> {
		
		private static final long serialVersionUID = 1L;
		public SyncReadMap(M delegate) {
			super(delegate);
		}
		
		@Override public ReadMap<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public Iterable<Entry<K, V>> entries(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries(key));
			}
		}
		@Override public V apply(K v) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.apply(v);
			}
		}
		@Override public V get(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.get(key);
			}
		}
		@Override public int size() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.size();
			}
		}
		@Override public UnitaryReadSet<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		}

		@Override
		public ReadSet<Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}

		@Override
		public ReadSet<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}		
		
	}
	
	protected class SyncAnyMap<K, V, M extends AnyMap<K, V>> implements AnyMap<K, V> {

		private static final long serialVersionUID = 1L;
		final M delegate;
		public SyncAnyMap(M delegate) {
			this.delegate = delegate;
		}

		@Override public V put(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.put(key, val);
			}
		}
		@Override public V putIfAbsent(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.putIfAbsent(key, val);
			}
		}
		@Override public boolean add(K key, V value) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.add(key, value);
			}
		}
		@Override public boolean contains(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.contains(key);
			}
		}
		@Override public boolean contains(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.contains(key, val);
			}
		}
		@Override public int count(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.count(key);
			}
		}
		@Override public int count(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.count(key, val);
			}
		}
		@Override public V first(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.first(key);
			}
		}
		@Override public List<V> list(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.list(key);
			}
		}
		@Override public int remove(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.remove(key);
			}
		}
		@Override public int remove(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.remove(key, val);
			}
		}
		@Override public AnyMap<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		@Override public Iterable<Entry<K, V>> removeAndReturn(K key) {
			synchronized (delegate) {
				return delegate.removeAndReturn(key);
			}
		}
		@Override public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.removeAndReturn(key, val);
			}
		}
		@Override public V removeAndReturnFirst(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.removeAndReturnFirst(key);
			}
		}
		@Override public AnySet<V> values() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values());
			}
		}
		@Override public int clear() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.clear();
			}
		}
		@Override public Iterable<Entry<K, V>> entries(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries(key));
			}
		}
		@Override public Iterator<Entry<K, V>> clearAndReturn() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.clearAndReturn();
			}
		}
		@Override public int totalCount() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.totalCount();
			}
		}
		@Override public int uniqueKeyCount() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.uniqueKeyCount();
			}
		}
		@Override public boolean isEmpty() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.isEmpty();
			}
		}
		@Override public boolean permitsDuplicateKeys() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.permitsDuplicateKeys();
			}
		}		
		@Override public AnyMap<V, K> inverse() {
			// if this is a BiMap, we need to wrap the inverse structure with the same synchronization object
			// otherwise we should wrap it in a new one. for safety default will be to wrap in the same.
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.inverse());
			}
		}
		@Override public AnySet<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		}
		@Override public AnySet<Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}
		@Override public AnySet<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}
		
		
	}
	
	protected class SyncMap<K, V> extends SyncAnyMap<K, V, Map<K, V>> implements Map<K, V> {
		
		private static final long serialVersionUID = -2945163785917823422L;
		public SyncMap(Map<K, V> delegate) {
			super(delegate);
		}
		
		@Override public V get(K key) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.get(key);
			}
		} 
		@Override public int size() {
			synchronized (SynchronizedDelegator.this) {
				return delegate.size();
			}
		}		
		@Override public V apply(K v) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.apply(v);
			}
		}
		@Override public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.putIfAbsent(key, putIfNotPresent);
			}
		}
		@Override public boolean replace(K key, V oldValue, V newValue) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.replace(key, oldValue, newValue);
			}
		}
		@Override public V replace(K key, V val) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.replace(key, val);
			}
		}
		@Override public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.ensureAndGet(key, putIfNotPresent);
			}
		}
		@Override public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) {
			synchronized (SynchronizedDelegator.this) {
				return delegate.ensureAndGet(key, putIfNotPresent);
			}
		}
		@Override public UnitarySet<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		}
		@Override public Set<Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}
		@Override public Set<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}
		@Override public Map<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		
	}
	
	protected class SyncMultiMap<K, V> extends SyncAnyMap<K, V, MultiMap<K, V>> implements MultiMap<K, V> {
		
		private static final long serialVersionUID = -2945163785917823422L;
		public SyncMultiMap(MultiMap<K, V> delegate) {
			super(delegate);
		}
		
		@Override public Set<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		}
		@Override public Set<Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}
		@Override public MultiSet<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}
		@Override public MultiMap<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}

		@Override
		public Iterable<V> apply(K v) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.apply(v));
			}
		}
		
	}
	
	protected class SyncListMap<K, V> extends SyncAnyMap<K, V, ListMap<K, V>> implements ListMap<K, V> {
		
		private static final long serialVersionUID = -2945163785917823422L;
		public SyncListMap(ListMap<K, V> delegate) {
			super(delegate);
		}
		
		@Override public MultiSet<V> values(K key) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.values(key));
			}
		}
		@Override public MultiSet<Entry<K, V>> entries() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.entries());
			}
		}
		@Override public MultiSet<K> keys() {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.keys());
			}
		}
		@Override public ListMap<K, V> copy() {
			synchronized (SynchronizedDelegator.this) {
				return new SynchronizedDelegator().wrap(delegate.copy());
			}
		}
		
		@Override
		public Iterable<V> apply(K v) {
			synchronized (SynchronizedDelegator.this) {
				return wrap(delegate.apply(v));
			}
		}
		
	}
	
	protected <V> Iterator<V> wrap(final Iterator<V> iter) {
		return new SyncIterator<V>(iter);
	}	
	protected <V> Iterable<V> wrap(final Iterable<V> iter) {
		return new SyncIterable<V, Iterable<V>>(iter);
	}
	protected <V> AnyReadSet<V> wrap(final AnyReadSet<V> delegate) {
		return new SyncAnyReadSet<V, AnyReadSet<V>>(delegate);
	}	
	protected <V> ReadSet<V> wrap(final ReadSet<V> delegate) {
		return new SyncReadSet<V, ReadSet<V>>(delegate);
	}	
	protected <V> UnitaryReadSet<V> wrap(final UnitaryReadSet<V> delegate) {
		return new SyncUnitaryReadSet<V, UnitaryReadSet<V>>(delegate);
	}	
	protected <V> AnySet<V> wrap(final AnySet<V> delegate) {
		return new SyncAnySet<V, AnySet<V>>(delegate);
	}	
	protected <V> UnitarySet<V> wrap(final UnitarySet<V> delegate) {
		return new SyncUnitarySet<V, UnitarySet<V>>(delegate);
	}	
	protected <V> Set<V> wrap(final Set<V> delegate) {
		return new SyncSet<V, Set<V>>(delegate);
	}	
	protected <V> MultiSet<V> wrap(final MultiSet<V> delegate) {
		return new SyncMultiSet<V, MultiSet<V>>(delegate);
	}	
	protected <K, V> AnyReadMap<K, V> wrap(final AnyReadMap<K, V> delegate) {
		return new SyncAnyReadMap<K, V, AnyReadMap<K, V>>(delegate);
	}	
	protected <K, V> ReadMap<K, V> wrap(final ReadMap<K, V> delegate) {
		return new SyncReadMap<K, V, ReadMap<K, V>>(delegate);
	}	
	protected <K, V> AnyMap<K, V> wrap(final AnyMap<K, V> delegate) {
		return new SyncAnyMap<K, V, AnyMap<K, V>>(delegate);
	}	
	protected <K, V> Map<K, V> wrap(final Map<K, V> delegate) {
		return new SyncMap<K, V>(delegate);
	}
	protected <K, V> MultiMap<K, V> wrap(final MultiMap<K, V> delegate) {
		return new SyncMultiMap<K, V>(delegate);
	}
	protected <K, V> ListMap<K, V> wrap(final ListMap<K, V> delegate) {
		return new SyncListMap<K, V>(delegate);
	}
	
	public static <V> Iterator<V> get(final Iterator<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> Iterable<V> get(final Iterable<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}
	public static <V> AnyReadSet<V> get(final AnyReadSet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> ReadSet<V> get(final ReadSet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> UnitaryReadSet<V> get(final UnitaryReadSet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> AnySet<V> get(final AnySet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> UnitarySet<V> get(final UnitarySet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> Set<V> get(final Set<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <V> MultiSet<V> get(final MultiSet<V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <K, V> AnyReadMap<K, V> get(final AnyReadMap<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <K, V> ReadMap<K, V> get(final ReadMap<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <K, V> AnyMap<K, V> get(final AnyMap<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}	
	public static <K, V> Map<K, V> get(final Map<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}
	public static <K, V> MultiMap<K, V> get(final MultiMap<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}
	public static <K, V> ListMap<K, V> get(final ListMap<K, V> delegate) {
		return new SynchronizedDelegator().wrap(delegate);
	}
	
}
