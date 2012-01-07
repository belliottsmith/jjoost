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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeEquality;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.Locality;
import org.jjoost.collections.iters.EmptyIterator;
import org.jjoost.collections.iters.UniformIterator;
import org.jjoost.util.Counter;
import org.jjoost.util.Equality;
import org.jjoost.util.Filter;
import org.jjoost.util.Filters;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;
import org.jjoost.util.Rehasher;
import org.jjoost.util.tuples.Value;

public class NestedMultiHashSet<V, N extends HashNode<N> & NestedMultiHashSet.INode<V, N>> implements MultiSet<V> {

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final HashStore<N> store;
	protected final Rehasher rehasher;
	protected final HashNodeFactory<V, N> nodeFactory;
	protected final ValueEquality<V, N> valEq;
	protected final Counter totalCount;
	private UniqueSet unique;
	
	protected NestedMultiHashSet(Counter counter, Rehasher rehasher, ValueEquality<V, N> equality, HashNodeFactory<V, N> nodeFactory, HashStore<N> table) {
		this.store = table;
		this.totalCount = counter;
		this.rehasher = rehasher;
		this.nodeFactory = nodeFactory;
		this.valEq = equality;
	}

	protected Function<N, N> nodeProj() {
		return Functions.<N>identity();
	}
	
	protected Function<Value<V>, V> valProj() {
		return Functions.<V>getValueContentsProjection();
	}
	
	public int capacity() {
		return store.capacity();
	}
	
	public void resize(int capacity) {
		store.resize(capacity);
	}
	
	final int hash(V key) {
		return rehasher.rehash(valEq.valEq.hash(key));
	}
	
	protected boolean removeNode(N node) {
		return store.removeNode(valProj(), valEq, node);
	}
	
	@Override
	public Equality<? super V> equality() {
		return valEq.valEq;
	}
	
	@Override
	public boolean permitsDuplicates() {
		return true;
	}

	@Override
	public boolean add(V val) {
		put(val);
		return true;
	}
	
	@Override
	public V put(V val) {
		final int hash = hash(val);
		while (true) {
			final N existing = store.putIfAbsent(hash, val, valEq, nodeFactory, nodeProj(), true);
			if (existing.put(val))
				break;
		}
		totalCount.add(1);
		return null;
	}
	
	@Override
	public void put(V val, int count) {
		final int hash = hash(val);
		while (true) {
			final N existing = store.putIfAbsent(hash, val, valEq, nodeFactory, nodeProj(), true);
			if (existing.put(val, count))
				break;
		}
		totalCount.add(count);
	}
	
	@Override
	public int putAll(Iterable<V> vals) {
		int c = 0;
		for (V val : vals)
			if (put(val) == null)
				c++;
		return c;
	}
	
	@Override
	public V putIfAbsent(V val) {
		final int hash = hash(val);
		while (true) {
			final N existing = store.putIfAbsent(hash, val, valEq, nodeFactory, nodeProj(), true);
			if (existing.initialise()) {				
				totalCount.add(1);
				return null;
			} else if (existing.valid()) {
				return existing.getValue();
			}
		}
	}
	
	@Override
	public int remove(V val) {
		final int hash = hash(val);
		while (true) {
			final N r = store.removeAndReturnFirst(hash, 1, val, valEq, nodeProj());
			if (r == null)
				return 0;
			final int removed = r.remove(Integer.MAX_VALUE);
			if (removed > 0) {
				totalCount.add(-removed);
				return removed;
			}
		}		
	}
	
	@Override
	public V removeAndReturnFirst(V val) {
		final int hash = hash(val);
		while (true) {
			final N r = store.removeAndReturnFirst(hash, 1, val, valEq, nodeProj());
			if (r == null)
				return null;
			final V v = r.getValue();
			final int removed = r.remove(Integer.MAX_VALUE);
			if (removed > 0) {
				totalCount.add(-removed);
				return v;
			}
		}
	}

	@Override
	public Iterable<V> removeAndReturn(V val) {
		final int hash = hash(val);
		while (true) {
			final N r = store.removeAndReturnFirst(hash, 1, val, valEq, nodeProj());
			if (r == null)
				return Iters.emptyIterable();
			final List<V> removed = r.removeAndReturn(Integer.MAX_VALUE);
			if (removed.size() > 0) {
				totalCount.add(-removed.size());
				return removed;
			}
		}
	}
	
	@Override
	public int remove(V val, int atMost) {
		if (atMost < 1) {
			if (atMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero elements");
			return 0;
		}
		final int hash = hash(val);
		while (true) {
			final N r = store.first(hash, val, valEq, nodeProj());
			if (r == null)
				return 0;
			final int removed = r.remove(atMost);
			if (removed != 0) {
				if (removed < atMost || !r.valid())
					store.removeNode(valProj(), valEq, r);
				totalCount.add(-removed);
				return removed;
			}
		}
	}
	
	@Override
	public V removeAndReturnFirst(V val, int atMost) {
		if (atMost < 1) {
			if (atMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero elements");
			return null;
		}
		final int hash = hash(val);
		while (true) {
			final N r = store.first(hash, val, valEq, nodeProj());
			if (r == null)
				return null;
			final int removed = r.remove(atMost);
			if (removed != 0) {
				if (removed < atMost || !r.valid())
					store.removeNode(valProj(), valEq, r);
				totalCount.add(-removed);
				return r.getValue();
			}
		}
	}

	@Override
	public Iterable<V> removeAndReturn(V val, int atMost) {
		if (atMost < 1) {
			if (atMost < 0)
				throw new IllegalArgumentException("Cannot remove less than zero elements");
			return Iters.emptyIterable();
		}
		final int hash = hash(val);
		while (true) {
			final N r = store.first(hash, val, valEq, nodeProj());
			if (r == null)
				return Iters.emptyIterable();
			final List<V> removed = r.removeAndReturn(atMost);
			if (removed.size() != 0) {
				if (removed.size() < atMost || !r.valid())
					store.removeNode(valProj(), valEq, r);
				totalCount.add(-removed.size());
				return removed;
			}
		}
	}
	
	@Override
	public boolean contains(V val) {
		final int hash = hash(val);
		while (true) {
			final N r = store.first(hash, val, valEq, nodeProj());
			if (r == null)
				return false;
			if (r.valid())
				return true;
		}		
	}
	@Override
	public int count(V val) {
		final int hash = hash(val);
		while (true) {
			final N r = store.first(hash, val, valEq, nodeProj());
			if (r == null)
				return 0;
			final int c = r.count();
			if (c >= 0)
				return c;
		}		
	}
	@Override
	public void shrink() {
		store.shrink();
	}
	@Override
	public V first(V val) {
		return store.first(hash(val), val, valEq, valProj());
	}
	@Override
	public List<V> list(V val) {
		return Iters.toList(all(val));
	}
	@Override
	public int totalCount() {
		return totalCount.get();
	}
	@Override
	public int uniqueCount() {
		return store.totalCount();
	}
	@Override
	public boolean isEmpty() {
		return store.isEmpty();
	}
	@Override
	public Iterable<V> all(final V val) {
		final int hash = hash(val);
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				final N n = store.first(hash, val, valEq, nodeProj());
				if (n == null)
					return EmptyIterator.<V>get();
				return n.iterator(NestedMultiHashSet.this);
			}
		};
	}

	@Override
	public MultiSet<V> copy() {
		
		// TODO implement
		// no way to guarantee totalCount is valid - must copy the table and then calculate a fresh total
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<V> unique() {
		if (unique == null)
			unique = new UniqueSet();
		return unique;
	}
	
	private final class UniqueSet extends AbstractUniqueSetAdapter<V> {
		
		private static final long serialVersionUID = -1106116714278629141L;

		@Override
		protected AnySet<V> set() {
			return NestedMultiHashSet.this;
		}

		@Override
		public Iterator<V> iterator() {
			return wrap(Functions.apply(valProj(), Filters.apply(VALID, store.unique(valProj(), valEq.getEquality(), Locality.ADJACENT, valProj(), valEq, nodeProj()))));
		}
		
	}
	
	private static final Filter<INode<?, ?>> VALID = new Filter<INode<?, ?>>() {
		private static final long serialVersionUID = -6361042110136400398L;
		@Override
		public boolean accept(INode<?, ?> test) {
			return test.valid();
		}
	};

	@Override
	public int clear() {
		int c = 0;
		final Iterator<N> iter = store.clearAndReturn(nodeProj());
		while (iter.hasNext()) {
			final N n = iter.next();
			final int r = n.remove(Integer.MAX_VALUE);
			totalCount.add(-r);
			c += r;
		}
		return c;
	}

	@Override
	public Iterator<V> clearAndReturn() {
		final List<Iterator<V>> ret = new ArrayList<Iterator<V>>();
		final Iterator<N> iter = store.clearAndReturn(nodeProj());
		while (iter.hasNext()) {
			final N n = iter.next();
			final int r = n.remove(Integer.MAX_VALUE);
			if (r != 0) {
				ret.add(new UniformIterator<V>(n.getValue(), r));
				totalCount.add(-r);
			}
		}
		return Iters.concat(ret.iterator());
	}

	@Override
	public Iterator<V> iterator() {
		final NodeContentsIterator<V, N> f = new NodeContentsIterator<V, N>(this);
		final Iterator<Iterator<V>> iters = store.all(valProj(), valEq, f);
		return Iters.concat(iters);
	}

	@Override
	public Boolean apply(V v) {
		return contains(v);
	}
	
	public String toString() {
		return "{" + Iters.toString(this.iterator(), ", ") + "}";
	}
	
	// **********************************
	// INTERFACES
	// **********************************
	
	public static interface INode<V, N extends HashNode<N> & INode<V, N>> extends Value<V> {
		public boolean put(V v, int count);
		public boolean put(V v);
		public boolean valid();
		public int count();
		public int remove(int target);
		public List<V> removeAndReturn(int target);
		public Iterator<V> iterator(NestedMultiHashSet<V, N> set);
		public boolean initialise();
	}

	public static final class ValueEquality<V, N extends HashNode<N> & INode<V, N>> implements HashNodeEquality<V, N> {
		protected final Equality<? super V> valEq;
		public ValueEquality(Equality<? super V> valEq) {
			this.valEq = valEq;
		}
		public final Equality<? super V> getEquality() {
			return valEq;
		}
		public final boolean isUnique() {
			return true;
		}
		@Override
		public boolean prefixMatch(V cmp, N n) {
			return valEq.equates(cmp, n.getValue());
		}
		@Override
		public boolean suffixMatch(V cmp, N n) {
			return true;
		}
	}
	
	// *******************************
	// UTILITY CLASSES
	// *******************************
	
//	private static final <V, N extends HashNode<N> & INode<V, N>> Destroyer<V, N> destroyer() {
//		return new Destroyer<V, N>();
//	}
//	
//	private static final class Destroyer<V, N extends HashNode<N> & INode<V, N>> implements Function<N, Iterator<V>> {
//		private static final long serialVersionUID = -965724235732791909L;
//		@Override
//		public Iterator<V> apply(N n) {
//			return n.removeAndReturn(Integer.MAX_VALUE).iterator();
//		}
//	}

	private static final class NodeContentsIterator<V, N extends HashNode<N> & INode<V, N>> implements Function<N, Iterator<V>> {
		private static final long serialVersionUID = -965724235732791909L;
		final NestedMultiHashSet<V, N> set;
		public NodeContentsIterator(NestedMultiHashSet<V, N> set) {
			this.set = set;
		}
		@Override
		public Iterator<V> apply(N n) {
			return n.iterator(set);
		}
	}

}
