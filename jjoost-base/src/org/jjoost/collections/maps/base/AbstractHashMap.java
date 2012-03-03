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

package org.jjoost.collections.maps.base;

import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeEquality;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.PutAction;
import org.jjoost.collections.iters.AbstractIterable;
import org.jjoost.collections.maps.ImmutableMapEntry;
import org.jjoost.collections.sets.base.AbstractSet;
import org.jjoost.collections.sets.base.IterableSet;
import org.jjoost.util.Equality;
import org.jjoost.util.Filters;
import org.jjoost.util.Function;
import org.jjoost.util.Functions;
import org.jjoost.util.Iters;
import org.jjoost.util.Rehasher;

public abstract class AbstractHashMap<K, V, N extends HashNode<N> & Map.Entry<K, V>, S extends HashStore<N, S>> implements AnyMap<K, V> {

	protected static abstract class NodeEquality<K, V, N extends HashNode<N> & Map.Entry<K, V>> implements HashNodeEquality<Entry<K, V>, N>, Equality<Entry<K, V>> {
		private static final long serialVersionUID = -4970889935020537472L;
    	protected final Equality<? super K> keyEq;
    	protected final Equality<? super V> valEq;
		public NodeEquality(Equality<? super K> keyEq,
				Equality<? super V> valEq) {
			this.keyEq = keyEq;
			this.valEq = valEq;
		}
		@Override
		public boolean equates(Entry<K, V> a, Entry<K, V> b) {
			return keyEq.equates(a.getKey(), b.getKey()) && valEq.equates(a.getValue(), b.getValue());
		}
		@Override
		public int hash(Entry<K, V> o) {
			return keyEq.hash(o.getKey());
		}		
	}
	
	protected static abstract class KeyEquality<K, V, N> implements HashNodeEquality<K, N> {
		private static final long serialVersionUID = 8275178097359756856L;
		protected final Equality<? super K> keyEq;
		public KeyEquality(Equality<? super K> keyEq) { this.keyEq = keyEq ; }
		@Override 
		public boolean suffixMatch(K key, N n) { return true ; }
		public Equality<? super K> getKeyEquality() { return keyEq ; }
	}	

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final S store;
	protected final Rehasher rehasher;
	protected final KeyEquality<K, V, N> keyEq;
	protected final NodeEquality<K, V, N> nodeEq;
	protected final HashMapNodeFactory<K, V, N> nodeFactory;
	protected IterableSet<V> valueSet;
	
	protected AbstractHashMap(
			Rehasher rehasher, 
			KeyEquality<K, V, N> keyEquality, 
			NodeEquality<K, V, N> entryEquality, 
			HashMapNodeFactory<K, V, N> nodeFactory, 
			S store) {
		this.store = store;
		this.rehasher = rehasher;
		this.keyEq = keyEquality;
		this.nodeEq = entryEquality;
		this.nodeFactory = nodeFactory;
	}

	protected final Entry<K, V> entry(K key, V val) {
		return new ImmutableMapEntry<K, V>(key, val);
	}
	
	protected final Function<Entry<K, V>, K> keyProj() {
		return Functions.<K, Entry<K, V>>getMapEntryKeyProjection();
	}
	
	protected final Function<Entry<K, V>, V> valProj() {
		return Functions.<V, Entry<K, V>>getMapEntryValueProjection();
	}
	
	protected final Function<N, N> nodeProj() {
		return Functions.<N>identity();
	}
	
	protected final Function<Entry<K, V>, Entry<K, V>> entryProj() {
		return Functions.<Entry<K, V>>identity();
	}
	
	public int capacity() {
		return store.capacity();
	}
	
	protected final int hash(K key) {
		return rehasher.rehash(keyEq.keyEq.hash(key));
	}
	
	@Override
	public int remove(K key, V val) {
		return store.remove(hash(key), Integer.MAX_VALUE, entry(key, val), nodeEq);
	}
	@Override
	public V removeAndReturnFirst(K key) {
		return store.removeAndReturnFirst(hash(key), Integer.MAX_VALUE, key, keyEq, valProj());
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return store.removeAndReturn(hash(key), Integer.MAX_VALUE, entry(key, val), nodeEq, entryProj());
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		return store.removeAndReturn(hash(key), Integer.MAX_VALUE, key, keyEq, entryProj());
	}
	@Override
	public int remove(K key) {
		return store.remove(hash(key), Integer.MAX_VALUE, key, keyEq);
	}
	@Override
	public boolean contains(K key, V val) {
		return store.count(hash(key), entry(key, val), nodeEq, 1) > 0;
	}
	@Override
	public boolean contains(K key) {
		return store.count(hash(key), key, keyEq, 1) > 0;
	}
	@Override
	public int count(K key, V val) {
		return store.count(hash(key), entry(key, val), nodeEq, Integer.MAX_VALUE);
	}
	@Override
	public int count(K key) {
		return store.count(hash(key), key, keyEq, Integer.MAX_VALUE);
	}
	@Override
	public Iterable<Entry<K, V>> entries(final K key) {
		final int hash = hash(key);
		return new Iterable<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return store.find(hash, key, keyEq, nodeProj(), nodeEq, entryProj());
			}
		};
	}
	@Override
	public V first(K key) {
		return store.first(hash(key), key, keyEq, valProj());
	}
	@Override
	public List<V> list(K key) {
		return store.findNow(hash(key), key, keyEq, valProj());
	}
	@Override
	public int totalCount() {
		return store.totalCount();
	}
	@Override
	public boolean isEmpty() {
		return store.isEmpty();
	}
	@Override
	public MultiSet<V> values() {
		if (valueSet == null)
			valueSet = new ValueSet();
		return valueSet;
	}

	@Override
	public int clear() {
		return store.clear();
	}
	
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		return store.clearAndReturn(entryProj());
	}
	
	@Override
	public AnyMap<V, K> inverse() {
		throw new UnsupportedOperationException();
	}
	
	protected final class ValueSet extends IterableSet<V> implements MultiSet<V> {
		private static final long serialVersionUID = -1124458438016390808L;
		@Override
		public Iterator<V> iterator() {
			return store.all(keyProj(), keyEq, valProj());
		}
		@Override
		public Equality<? super V> equality() {
			return nodeEq.valEq ;
		}
		@Override
		public void put(V val, int numberOfTimes) {
			throw new UnsupportedOperationException();
		}
	};

	protected abstract class AbstractKeyValueSet extends AbstractSet<V> implements AnySet<V> {
		
		private static final long serialVersionUID = 1461826147890179114L;
		
		protected final int hash;
		protected final K key;
		
		public AbstractKeyValueSet(K key) {
			this.key = key;
			this.hash = hash(key);
		}
		
		@Override
		public Equality<? super V> equality() {
			return nodeEq.valEq;
		}
		
		@Override
		public boolean contains(V value) {
			return store.count(hash, entry(key, value), nodeEq, 1) > 0;
		}
		
		@Override
		public int count(V value) {
			return store.count(hash, entry(key, value), nodeEq, Integer.MAX_VALUE);
		}
		
		@Override
		public int totalCount() {
			return store.count(hash, key, keyEq, Integer.MAX_VALUE);
		}
		
		@Override
		public String toString() {
			return "{" + Iters.toString(this, ", ") + "}";
		}
		
		@Override
		public int clear() {
			return store.remove(hash, Integer.MAX_VALUE, key, keyEq);
		}
		
		@Override
		public Iterator<V> clearAndReturn() {
			return store.removeAndReturn(hash, Integer.MAX_VALUE, key, keyEq, valProj()).iterator();
		}
		
		@Override
		public Boolean apply(V v) {
			return store.count(hash, entry(key, v), nodeEq, 1) > 0;
		}
		
		@Override
		public Iterable<V> all(final V v) {
			return new AbstractIterable<V>() {
				@Override
				public Iterator<V> iterator() {
					return store.find(hash, entry(key, v), nodeEq, entryProj(), nodeEq, valProj());
				}
			};
		}
		
		@Override
		public V first(final V val) {
			return store.first(hash, entry(key, val), nodeEq, valProj());
		}
		
		@Override
		public List<V> list(final V val) {
			return store.findNow(hash, entry(key, val), nodeEq, valProj());
		}
		
		@Override
		public Iterator<V> iterator() {
			return store.find(hash, key, keyEq, entryProj(), nodeEq, valProj());
		}
		
		@Override
		public boolean isEmpty() {
			return store.count(hash, key, keyEq, 1) > 0;
		}
		
		@Override
		public int uniqueCount() {
			if (nodeEq.isUnique())
				return totalCount();
			return Iters.count(Filters.apply(Filters.unique(nodeEq.valEq), iterator()));
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !nodeEq.isUnique();
		}
		
		@Override
		public boolean add(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException();
			final N insert = nodeFactory.makeNode(hash, key, val);
			return store.put(PutAction.IFABSENT, insert, insert, nodeEq, entryProj()) == null;
		}
		
		@Override
		public V put(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException();
			final N insert = nodeFactory.makeNode(hash, key, val);
			return store.put(PutAction.PUT, insert, insert, nodeEq, valProj());
		}
		
		@Override
		public V putIfAbsent(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException();
			final N insert = nodeFactory.makeNode(hash, key, val);
			return store.put(PutAction.IFABSENT, insert, insert, nodeEq, valProj());
		}
		
		@Override
		public Iterable<V> removeAndReturn(V val) {
			return store.removeAndReturn(hash, Integer.MAX_VALUE, entry(key, val), nodeEq, valProj());
		}
		
		@Override
		public int remove(V val) {
			return store.remove(hash, Integer.MAX_VALUE, entry(key, val), nodeEq);
		}
		
		@Override
		public V removeAndReturnFirst(V val) {
			return store.removeAndReturnFirst(hash, Integer.MAX_VALUE, entry(key, val), nodeEq, valProj());
		}
		
		@Override
		public int putAll(Iterable<V> vals) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int remove(V val, int atMost) {
			return store.remove(hash, atMost, entry(key, val), nodeEq);
		}
		
		@Override
		public V removeAndReturnFirst(V val, int atMost) {
			return store.removeAndReturnFirst(hash, atMost, entry(key, val), nodeEq, valProj());
		}
		
		@Override
		public Iterable<V> removeAndReturn(V val, int atMost) {
			return store.removeAndReturn(hash, atMost, entry(key, val), nodeEq, valProj());
		}
		
	}
	
	protected abstract class AbstractKeySet extends AbstractSet<K> implements AnySet<K> {
		
		private static final long serialVersionUID = 1461826147890179114L;

		@Override
		public Equality<? super K> equality() {
			return nodeEq.keyEq;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !keyEq.isUnique();
		}
		@Override
		public boolean contains(K value) {
			return AbstractHashMap.this.contains(value);
		}

		@Override
		public int count(K value) {
			return AbstractHashMap.this.count(value);
		}

		@Override
		public int totalCount() {
			return AbstractHashMap.this.totalCount();
		}

		@Override
		public int clear() {
			return AbstractHashMap.this.clear();
		}
		
		@Override
		public Iterator<K> clearAndReturn() {
			return store.clearAndReturn(Functions.<K, Entry<K, V>>getMapEntryKeyProjection());
		}

		@Override
		public Boolean apply(K v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE;
		}

		@Override
		public Iterable<K> all(final K key) {
			final int hash = hash(key);
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.find(hash, key, keyEq, nodeProj(), nodeEq, keyProj());
				}
			};
		}
		
		@Override
		public K first(final K key) {
			return store.first(hash(key), key, keyEq, keyProj());
		}
		
		@Override
		public List<K> list(final K key) {
			return store.findNow(hash(key), key, keyEq, keyProj());
		}
		
		@Override
		public Iterator<K> iterator() {
			return store.all(keyProj(), keyEq, keyProj());
		}

		@Override
		public boolean isEmpty() {
			return AbstractHashMap.this.isEmpty();
		}

		@Override
		public int uniqueCount() {
			return AbstractHashMap.this.uniqueKeyCount();
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
		public final K putIfAbsent(K val) {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Iterable<K> removeAndReturn(K key) {
			return store.removeAndReturn(hash(key), Integer.MAX_VALUE, key, keyEq, keyProj());
		}
		
		@Override
		public int remove(K key) {
			return store.remove(hash(key), Integer.MAX_VALUE, key, keyEq);
		}

		@Override
		public K removeAndReturnFirst(K key) {
			return store.removeAndReturnFirst(hash(key), Integer.MAX_VALUE, key, keyEq, keyProj());
		}
		
		@Override
		public int putAll(Iterable<K> vals) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int remove(K key, int atMost) {
			return store.remove(hash(key), atMost, key, keyEq);
		}
		
		@Override
		public K removeAndReturnFirst(K key, int atMost) {
			return store.removeAndReturnFirst(hash(key), atMost, key, keyEq, keyProj());
		}
		
		@Override
		public Iterable<K> removeAndReturn(K key, int atMost) {
			return store.removeAndReturn(hash(key), atMost, key, keyEq, keyProj());
		}
		
		public String toString() {
			return "{" + Iters.toString(this, ", ") + "}";
		}
		
	}
	
	protected abstract class AbstractEntrySet extends AbstractSet<Entry<K, V>> implements AnySet<Entry<K, V>> {
		
		private static final long serialVersionUID = 4037233101289518536L;
		
		@Override
		public Equality<? super Entry<K, V>> equality() {
			return nodeEq;
		}
		
		@Override
		public boolean add(Entry<K, V> entry) {
			return put(entry) == null;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !nodeEq.isUnique();
		}

		@Override
		public boolean contains(Entry<K, V> value) {
			return AbstractHashMap.this.contains(value.getKey(), value.getValue());
		}

		@Override
		public int count(Entry<K, V> value) {
			return AbstractHashMap.this.count(value.getKey(), value.getValue());
		}

		@Override
		public int totalCount() {
			return AbstractHashMap.this.totalCount();
		}

		@Override
		public int clear() {
			return AbstractHashMap.this.clear();
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return AbstractHashMap.this.clearAndReturn();
		}

		@Override
		public Boolean apply(Entry<K, V> v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE;
		}
		
		@Override
		public Iterable<Entry<K, V>> all(final Entry<K, V> entry) {
			final int hash = hash(entry.getKey());
			return (Iterable<Entry<K, V>>) 
			new AbstractIterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.find(hash, entry, nodeEq, nodeProj(), nodeEq, entryProj());
				}
			};
		}

		@Override
		public Entry<K, V> first(Entry<K, V> entry) {
			return store.first(hash(entry.getKey()), entry, nodeEq, nodeProj());
		}

		@Override
		public List<Entry<K, V>> list(Entry<K, V> entry) {
			return store.findNow(hash(entry.getKey()), entry, nodeEq, entryProj());
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return store.all(keyProj(), keyEq, entryProj());
		}
		
		@Override
		public boolean isEmpty() {
			return AbstractHashMap.this.isEmpty();
		}
		
		@Override
		public int uniqueCount() {
			return AbstractHashMap.this.totalCount();
		}

		@Override
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0;
			for (final Entry<K, V> val : vals)
				if (put(val) == null)
					c++;
			return c;
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry, int atMost) {
			return store.removeAndReturnFirst(hash(entry.getKey()), atMost, entry, nodeEq, entryProj());
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry, int atMost) {
			return store.remove(hash(entry.getKey()), atMost, entry, nodeEq);
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry, int atMost) {
			return store.removeAndReturn(hash(entry.getKey()), atMost, entry, nodeEq, entryProj());
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry) {
			return store.removeAndReturnFirst(hash(entry.getKey()), Integer.MAX_VALUE, entry, nodeEq, entryProj());
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry) {
			return store.remove(hash(entry.getKey()), Integer.MAX_VALUE, entry, nodeEq);
		}
		
		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry) {
			return store.removeAndReturn(hash(entry.getKey()), Integer.MAX_VALUE, entry, nodeEq, entryProj());
		}
		
		public String toString() {
			return "{" + Iters.toString(this, ", ") + "}";
		}
		
	}
	
	public String toString() {
		return store.toString();
	}

	public boolean equals(Object tht) {
		if (!(tht instanceof AnyMap)) {
			return false;
		}
		final AnyMap<?, ?> that1 = (AnyMap<?, ?>) tht;
		if (!this.keys().equality().equals(that1.keys().equality())) {
			return false;
		}
		if (!this.values().equality().equals(that1.values().equality())) {
			return false;
		}
		final AnyMap<K, V> that = (AnyMap<K, V>) that1;
		final Iterator<? extends Map.Entry<K, V>> a = (Iterator<? extends Entry<K, V>>) that.entries().iterator();
		final Iterator<? extends Map.Entry<K, V>> b = this.entries().iterator();
		while (a.hasNext() && b.hasNext()) {
			final Entry<K, V> ae = a.next();
			final Entry<K, V> be = b.next();
			if (!keyEq.getKeyEquality().equates(ae.getKey(), be.getKey())) {
				return false;
			}
			if (!values().equality().equates(ae.getValue(), be.getValue())) {
				return false;
			}
		}
		return a.hasNext() == b.hasNext(); 
	}
	
}
