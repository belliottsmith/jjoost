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

import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.util.Equality;
import org.jjoost.util.Filters;
import org.jjoost.util.Iters;

public abstract class IterableSet<V> extends AbstractSet<V> implements MultiSet<V> {

	private static final long serialVersionUID = 7475686519443650191L;
	
	public static class ConcreteIterableSet<V> extends IterableSet<V> {
		
		private static final long serialVersionUID = 5370957005133369394L;
		
		private final Equality<? super V> eq;
		private final Iterable<V> iter;
		
		public ConcreteIterableSet(Equality<? super V> eq, Iterable<V> iter) {
			this.eq = eq;
			this.iter = iter;
		}
		@Override
		public Equality<? super V> equality() {
			return eq;
		}
		@Override
		public Iterator<V> iterator() {
			return iter.iterator();
		}
		
	}

	public abstract Equality<? super V> equality();
	public abstract Iterator<V> iterator();
	
	private UniqueIterableSet unique;

	@Override
	public Boolean apply(V v) {
		return Iters.contains(equality(), v, iterator());
	}

	@Override
	public int clear() {
		return Iters.count(clearAndReturn());
	}

	@Override
	public Iterator<V> clearAndReturn() {
		return Iters.destroyAsConsumed(iterator());
	}

	@Override
	public MultiSet<V> copy() {
		final Iterable<V> copy = Iters.toList(iterator());
		return new ConcreteIterableSet<V>(equality(), copy);
	}
	
	@Override
	public void put(V val, int numberOfTimes) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int remove(V value, int removeAtMost) {
		return Filters.remove(Filters.isEqualTo(value, equality()), removeAtMost, iterator());
	}

	@Override
	public int remove(V value) {
		return remove(value, Integer.MAX_VALUE);
	}

	@Override
	public Iterable<V> removeAndReturn(V value, int removeAtMost) {
		return Iters.toList(Filters.removeAndReturn(Filters.isEqualTo(value, equality()), removeAtMost, iterator()));
	}

	@Override
	public Iterable<V> removeAndReturn(V value) {
		return removeAndReturn(value, Integer.MAX_VALUE);
	}

	@Override
	public V removeAndReturnFirst(V value, int removeAtMost) {
		return Filters.removeAndReturnFirst(Filters.isEqualTo(value, equality()), removeAtMost, iterator());
	}

	@Override
	public V removeAndReturnFirst(V value) {
		return removeAndReturnFirst(value, Integer.MAX_VALUE);
	}

	@Override
	public Iterable<V> all(final V value) {
		return Filters.apply(Filters.isEqualTo(value, equality()), this);
	}

	@Override
	public boolean contains(V value) {
		return Iters.contains(equality(), value, iterator());
	}

	@Override
	public int count(V value) {
		return Iters.count(equality(), value, iterator());
	}

	@Override
	public V first(V value) {
		final Iterator<V> iter = Filters.apply(Filters.isEqualTo(value, equality()), iterator());
		return iter.hasNext() ? iter.next() : null;
	}

	@Override
	public boolean isEmpty() {
		return !iterator().hasNext();
	}

	@Override
	public List<V> list(V value) {
		return Iters.toList(all(value));
	}

	@Override
	public boolean permitsDuplicates() {
		return true;
	}

	@Override
	public int totalCount() {
		return Iters.count(iterator());
	}

	@Override
	public Set<V> unique() {
		if (unique == null) {
			unique = new UniqueIterableSet();
		}
		return unique;
	}

	@Override
	public int uniqueCount() {
		return Iters.count(unique());
	}

	private final class UniqueIterableSet extends AbstractUniqueSetAdapter<V> implements Set<V> {

		private static final long serialVersionUID = -8170697306505507966L;

		@Override
		protected AnySet<V> set() {
			return IterableSet.this;
		}

		@Override
		public Iterator<V> iterator() {
			return Filters.apply(Filters.unique(equality()), IterableSet.this.iterator());
		}
		
	}
	
}
