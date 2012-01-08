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
import java.util.Map.Entry;

import org.jjoost.collections.AnySet;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Set;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.Locality;
import org.jjoost.collections.base.HashStore.PutAction;
import org.jjoost.collections.sets.base.AbstractUniqueSetAdapter;
import org.jjoost.util.Equality;
import org.jjoost.util.Rehasher;

public class InlineMultiHashMap<K, V, N extends HashNode<N> & Entry<K, V>> extends AbstractHashMap<K, V, N> implements MultiMap<K, V> {

	protected InlineMultiHashMap(
			Rehasher rehasher, 
			AbstractHashMap.KeyEquality<K, V, N> keyEquality, 
			AbstractHashMap.NodeEquality<K, V, N> entryEquality,
			HashMapNodeFactory<K, V, N> nodeFactory, HashStore<N> table) {
		super(rehasher, keyEquality, entryEquality, nodeFactory, table);
	}
	
	private static final long serialVersionUID = -6385620376018172675L;

	private Set<Entry<K, V>> entrySet;
	private MultiSet<K> keySet;
	
	@Override
	public Set<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		Set<Entry<K, V>> r = entrySet;
		if (r == null)
			entrySet = r = new EntrySet();
		return r;
	}
	@Override
	public MultiSet<K> keys() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		MultiSet<K> r = keySet;
		if (r == null) {
			keySet = r = new KeySet();
		}
		return r;
	}
	
	@Override
	public int uniqueKeyCount() {
		return store.uniquePrefixCount();
	}
	
	@Override
	public Set<V> values(K k) {
		return new KeyValueSet(k);
	}
	
	@Override
	public Iterable<V> apply(K v) {
		return values(v);
	}

	@Override
	public boolean add(K key, V val) {
		final N n = nodeFactory.makeNode(hash(key), key, val);
		return store.put(PutAction.IFABSENT, n, n, nodeEq, nodeProj()) == null;
	}
	
	@Override
	public V put(K key, V val) {
		final N n = nodeFactory.makeNode(hash(key), key, val);
		return store.put(PutAction.PUT, n, n, nodeEq, valProj());
	}

	@Override
	public V putIfAbsent(K key, V val) {
		final N n = nodeFactory.makeNode(hash(key), key, val);
		return store.put(PutAction.IFABSENT, n, n, nodeEq, valProj());
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return true;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		return new InlineMultiHashMap<K, V, N>(rehasher, keyEq, nodeEq, nodeFactory, store.copy(nodeProj(), nodeEq));
	}

	final class KeyValueSet extends AbstractKeyValueSet implements Set<V> {
		private static final long serialVersionUID = 2741936401896784235L;
		public KeyValueSet(K key) {
			super(key);
		}
		@Override
		public Set<V> copy() {
			throw new UnsupportedOperationException();
		}
		@Override
		public V get(V key) {
			return first(key);
		}
		@Override
		public int size() {
			return totalCount();
		}
		@Override
		public Set<V> unique() {
			return this;
		}
	}
	
	final class KeySet extends AbstractKeySet implements MultiSet<K> {

		private static final long serialVersionUID = 2741936401896784235L;
		private UniqueKeySet unique;
		
		@Override
		public MultiSet<K> copy() {
			throw new UnsupportedOperationException();
		}
		@Override
		public void put(K val, int numberOfTimes) {
			throw new UnsupportedOperationException();
		}
		
		@Override 
		public Set<K> unique() {
			if (unique == null)
				unique = new UniqueKeySet();
			return unique;
		}
		
		private final class UniqueKeySet extends AbstractUniqueSetAdapter<K> {
			private static final long serialVersionUID = 686867617922872433L;
			@Override
			protected AnySet<K> set() {
				return KeySet.this;
			}
			@Override
			public Iterator<K> iterator() {
				return store.unique(keyProj(), keyEq.keyEq, Locality.ADJACENT, nodeProj(), nodeEq, keyProj());
			}
		}
		
	}

	final class EntrySet extends AbstractEntrySet implements Set<Entry<K, V>> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			final K key = entry.getKey();
			final V val = entry.getValue();
			final N n = nodeFactory.makeNode(hash(key), key, val);
			return store.put(PutAction.PUT, n, n, nodeEq, entryProj());
		}
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final K key = entry.getKey();
			final V val = entry.getValue();
			final N n = nodeFactory.makeNode(hash(key), key, val);
			return store.put(PutAction.IFABSENT, n, n, nodeEq, entryProj());
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
		@Override
		public Set<Entry<K, V>> unique() {
			return this;
		}
	}

	// *****************************************
	// EQUALITY
	// *****************************************
	

	public static abstract class NodeEquality<K, V, N extends HashNode<N> & Entry<K, V>> extends AbstractHashMap.NodeEquality<K, V, N> {
		private static final long serialVersionUID = -5082864991691726065L;
		public NodeEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq);
		}
		@Override
		public boolean isUnique() {
			return true;
		}
	}	
	
	public static abstract class KeyEquality<K, V, N> extends AbstractHashMap.KeyEquality<K, V, N> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq);
		}
		@Override
		public boolean isUnique() {
			return false;
		}
	}	

}
