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

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Equality;

public abstract class SynchronizedDelegator {

	protected <V> Iterable<V> wrap(final Iterable<V> iter) {
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				synchronized (SynchronizedDelegator.this) {
					return wrap(iter.iterator());
				}
			}
		};
	}
	
	protected <V> Iterator<V> wrap(final Iterator<V> iter) {
		return new Iterator<V>() {
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
		};
	}

	protected <V> AnySet<V> wrap(final AnySet<V> delegate) {
		return new AnySet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public boolean contains(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.contains(value);
				}
			}
			@Override public Set<V> copy() {
				throw new UnsupportedOperationException();
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
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.iterator());
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
			@Override public void shrink() {
				synchronized(SynchronizedDelegator.this) {
					delegate.shrink();
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.totalCount();
				}
			}
			@Override public AnySet<V> unique() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.unique());
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.uniqueCount();
				}
			}
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.remove(value, removeAtMost);
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturn(value, removeAtMost);
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost);
				}
			}
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.equality();
				}
			}
			@Override
			public boolean add(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.add(value);
				}
			}
		};
	}
	
	protected <V> UnitarySet<V> wrap(final UnitarySet<V> delegate) {
		return new UnitarySet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public boolean contains(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.contains(value);
				}
			}
			@Override public UnitarySet<V> copy() {
				throw new UnsupportedOperationException();
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
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.iterator());
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
			@Override public void shrink() {
				synchronized(SynchronizedDelegator.this) {
					delegate.shrink();
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.totalCount();
				}
			}
			@Override public UnitarySet<V> unique() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.unique());
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.uniqueCount();
				}
			}
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.remove(value, removeAtMost);
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturn(value, removeAtMost);
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost);
				}
			}
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.equality();
				}
			}
			@Override
			public V get() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.get();
				}
			}
			@Override
			public boolean add(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.add(value);
				}
			}
		};
	}
	
	protected <V> Set<V> wrap(final Set<V> delegate) {
		return new Set<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public boolean contains(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.contains(value);
				}
			}
			@Override public Set<V> copy() {
				throw new UnsupportedOperationException();
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
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.iterator());
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
			@Override public void shrink() {
				synchronized(SynchronizedDelegator.this) {
					delegate.shrink();
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.totalCount();
				}
			}
			@Override public Set<V> unique() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.unique());
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.uniqueCount();
				}
			}
			@Override
			public V get(V key) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.get(key);
				}
			}
			@Override public int size() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.size();
				}
			}
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.remove(value, removeAtMost);
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturn(value, removeAtMost);
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost);
				}
			}
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.equality();
				}
			}
			@Override
			public boolean add(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.add(value);
				}
			}
		};
	}
	
	protected <V> MultiSet<V> wrap(final MultiSet<V> delegate) {
		return new MultiSet<V>() {
			private static final long serialVersionUID = -4043870977539052035L;
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
			@Override public boolean contains(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.contains(value);
				}
			}
			@Override public MultiSet<V> copy() {
				throw new UnsupportedOperationException();
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
			@Override public Iterator<V> iterator() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.iterator());
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
			@Override public V put(V val) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.put(val);
				}
			}
			@Override public void put(V val, int count) {
				synchronized(SynchronizedDelegator.this) {
					delegate.put(val, count);
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
			@Override public void shrink() {
				synchronized(SynchronizedDelegator.this) {
					delegate.shrink();
				}
			}
			@Override public int totalCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.totalCount();
				}
			}
			@Override public Set<V> unique() {
				synchronized(SynchronizedDelegator.this) {
					return wrap(delegate.unique());
				}
			}
			@Override public int uniqueCount() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.uniqueCount();
				}
			}			
			@Override
			public int remove(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.remove(value, removeAtMost);
				}
			}
			@Override
			public Iterable<V> removeAndReturn(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturn(value, removeAtMost);
				}
			}
			@Override
			public V removeAndReturnFirst(V value, int removeAtMost) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.removeAndReturnFirst(value, removeAtMost);
				}
			}
			@Override
			public Equality<? super V> equality() {
				synchronized(SynchronizedDelegator.this) {
					return delegate.equality();
				}
			}
			@Override
			public boolean add(V value) {
				synchronized(SynchronizedDelegator.this) {
					return delegate.add(value);
				}
			}
		};
	}
	
}
