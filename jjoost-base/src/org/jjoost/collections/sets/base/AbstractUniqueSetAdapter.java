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

package org.jjoost.collections.sets.base;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.Set;
import org.jjoost.collections.lists.UniformList;
import org.jjoost.util.Equality;
import org.jjoost.util.Filters;
import org.jjoost.util.Iters;
import org.jjoost.util.Objects;

public abstract class AbstractUniqueSetAdapter<V> extends AbstractSet<V> implements Set<V> {

	private static final long serialVersionUID = -4614054305733007946L;

	protected abstract AnySet<V> set();
	
	public AbstractUniqueSetAdapter() { }

	protected Iterator<V> wrap(Iterator<V> iter) {
		return new UniqueSetIterator(iter);
	}
	
	protected Iterable<V> wrap(Iterable<V> iter) {
		return new UniqueSetIterable(iter);
	}
	
	protected Iterator<V> uniq(Iterator<V> iter) {
		return Filters.apply(iter, Filters.unique(set().equality()));
	}
	
	protected Iterable<V> uniq(Iterable<V> iter) {
		return Filters.apply(iter, Filters.unique(set().equality()));
	}
	
	@Override
	public Set<V> copy() {
		return (Set<V>) set().copy().unique();
	}

	@Override
	public V get(V key) {
		return set().first(key);
	}

	@Override
	public boolean add(V val) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public V put(V val) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int putAll(Iterable<V> vals) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V putIfAbsent(V val) {
		return set().putIfAbsent(val);
	}

	@Override
	public int size() {
		return set().uniqueCount();
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn());
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return uniq(set().clearAndReturn());
	}

	@Override
	public Iterable<V> all(V value) {
		return wrap(uniq(set().all(value)));
	}

	@Override
	public List<V> list(V value) {
		final V find = first(value);
		if (find == null) {
			if (value == null) {
				return new UniformList<V>(null, set().contains(null) ? 1 : 1);
			} else {
				return Collections.emptyList();
			}
		} else {
			return new UniformList<V>(find, 1);
		}
	}

	@Override
	public int remove(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return Math.min(1, set().remove(value, removeAtMost));
		return Math.min(1, set().remove(value));
	}

	@Override
	public int remove(V value) {
		return Math.min(1, set().remove(value));
	}

	@Override
	public Iterable<V> removeAndReturn(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return uniq(set().removeAndReturn(value, removeAtMost));
		return uniq(set().removeAndReturn(value));
	}

	@Override
	public Iterable<V> removeAndReturn(V value) {
		return uniq(set().removeAndReturn(value));
	}

	@Override
	public V removeAndReturnFirst(V value, int removeAtMost) {
		if (removeAtMost < 1) 			
			return set().removeAndReturnFirst(value, removeAtMost);
		return set().removeAndReturnFirst(value);
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return set().removeAndReturnFirst(value);
	}

	@Override
	public boolean contains(V value) {
		return set().contains(value);
	}

	@Override
	public int count(V value) {
		return set().contains(value) ? 1 : 0;
	}

	@Override
	public Equality<? super V> equality() {
		return set().equality();
	}

	@Override
	public V first(V value) {
		return set().first(value);
	}

	@Override
	public boolean isEmpty() {
		return set().isEmpty();
	}

	@Override
	public boolean permitsDuplicates() {
		return false;
	}

	@Override
	public int totalCount() {
		return set().uniqueCount();
	}

	@Override
	public Set<V> unique() {
		return this;
	}

	@Override
	public int uniqueCount() {
		return set().uniqueCount();
	}

	@Override
	public Boolean apply(V v) {
		return set().apply(v);
	}

	private final class UniqueSetIterator implements Iterator<V> {
		final Iterator<? extends V> wrapped;
		V prev = Objects.initialisationSentinelWithObjectErasure();
		public UniqueSetIterator(Iterator<? extends V> base) {
			this.wrapped = base;
		}
		public V next() {
			final V next = wrapped.next();
			prev = next;
			return next;
		}
		public void remove() {
			if (Objects.isInitialisationSentinelWithObjectErasure(prev))
				throw new NoSuchElementException();
			wrapped.remove();
			set().remove(prev);
		}
		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}
	}
	
	private final class UniqueSetIterable implements Iterable<V> {
		final Iterable<? extends V> wrapped;
		public UniqueSetIterable(Iterable<? extends V> base) {
			this.wrapped = base;
		}
		@Override
		public Iterator<V> iterator() {
			return new UniqueSetIterator(wrapped.iterator());
		}
	}
	
}
