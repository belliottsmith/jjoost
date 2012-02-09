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

import java.util.Map.Entry;

import org.jjoost.collections.Map;
import org.jjoost.collections.Set;
import org.jjoost.collections.UnitarySet;
import org.jjoost.collections.base.HashNode;
import org.jjoost.collections.base.HashNodeFactory;
import org.jjoost.collections.base.HashStore;
import org.jjoost.collections.base.HashStore.PutAction;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Rehasher;

public class HashMap<K, V, N extends HashNode<N> & Entry<K, V>, S extends HashStore<N, S>> extends AbstractHashMap<K, V, N, S> implements Map<K, V> {

	protected HashMap(
			Rehasher rehasher, 
			AbstractHashMap.KeyEquality<K, V, N> keyEquality, 
			AbstractHashMap.NodeEquality<K, V, N> entryEquality,
			HashMapNodeFactory<K, V, N> nodeFactory, S table) {
		super(rehasher, keyEquality, entryEquality, nodeFactory, table);
	}
	
	private static final long serialVersionUID = -6385620376018172675L;

	private Set<Entry<K, V>> entrySet;
	private Set<K> keySet;
	
	@Override
	public Set<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		Set<Entry<K, V>> r = entrySet;
		if (r == null)
			entrySet = r = new EntrySet();
		return r;
	}
	@Override
	public Set<K> keys() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		Set<K> r = keySet;
		if (r == null) {
			keySet = r = new KeySet();
		}
		return r;
	}
	
	@Override
	public boolean add(K key, V val) {
		return store.put(PutAction.IFABSENT, key, nodeFactory.makeNode(hash(key), key, val), keyEq, nodeProj()) == null;
	}
	
	@Override
	public V put(K key, V val) {
		return store.put(PutAction.PUT, key, nodeFactory.makeNode(hash(key), key, val), keyEq, valProj());
	}

	@Override
	public V putIfAbsent(K key, V val) {
		return store.put(PutAction.IFABSENT, key, nodeFactory.makeNode(hash(key), key, val), keyEq, valProj());
	}

	@Override
	public V replace(K key, V val) {
		N n = store.first(hash(key), key, keyEq, nodeProj());
		if (n != null && (n = store.put(PutAction.REPLACE, n, nodeFactory.makeNode(hash(key), key, val), nodeEq, nodeProj())) != null) {
			return n.getValue();
		}
		return null;
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return store.put(PutAction.PUT, nodeFactory.makeNode(hash(key), key, oldValue), nodeFactory.makeNode(hash(key), key, newValue), nodeEq, nodeProj()) != null;
	}
	
	@Override
	public V ensureAndGet(K key, final Factory<? extends V> putIfNotPresent) {
		return store.put(PutAction.ENSUREANDGET, hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 1L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.makeNode(hash, key, putIfNotPresent.create());
			}
		}, valProj());
	}

	@Override
	public V ensureAndGet(K key, final Function<? super K, ? extends V> putIfNotPresent) {
		return store.put(PutAction.ENSUREANDGET, hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 526770033919300687L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.makeNode(hash, key, putIfNotPresent.apply(key));
			}
		}, valProj());
	}

	@Override
	public UnitarySet<V> values(K key) {
		return new KeyValueSet(key);
	}	

	@Override
	public V get(K key) {
		return first(key);
	}

	@Override
	public V apply(K k) {
		return first(k);
	}
	
	@Override
	public V putIfAbsent(final K key, final Function<? super K, ? extends V> putIfNotPresent) {
		return store.put(PutAction.ENSUREANDGET, hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 3624491527804791117L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.makeNode(hash, key, putIfNotPresent.apply(key));
			}
		}, valProj());
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return false;
	}
	
	@Override
	public int size() {
		return totalCount();
	}

	@Override
	public int uniqueKeyCount() {
		return totalCount();
	}
	
	final class KeyValueSet extends AbstractKeyValueSet implements UnitarySet<V> {
		
		private static final long serialVersionUID = 2741936401896784235L;
		
		public KeyValueSet(K key) {
			super(key);
		}
		
		@Override
		public V get() {
			return HashMap.this.first(key);
		}
		
		@Override
		public UnitarySet<V> copy() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public UnitarySet<V> unique() {
			return this;
		}
		
		@Override
		public final boolean add(V val) {
			final N replaced = _put(PutAction.PUT, val);
			if (replaced != null) {
				return false;
			}
			return true;
		}
		
		@Override
		public final V put(V val) {
			final N replaced = _put(PutAction.PUT, val);
			if (replaced != null) {
				return replaced.getValue();
			}
			return null;
		}
		
		@Override
		public final V putIfAbsent(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException();
			final N insert = nodeFactory.makeNode(hash, key, val);
			return store.put(PutAction.IFABSENT, insert, insert, nodeEq, valProj());
		}
		
		protected final N _put(PutAction action, V val) {
			final N insert = nodeFactory.makeNode(hash, key, val);
			if (store.put(action, key, insert, keyEq, nodeProj()) != null) {
				final N replaced = store.put(action, key, insert, keyEq, nodeProj());
				if (replaced != null) {
					return replaced;
				}
				throw new IllegalStateException("This set can only contain one value at a time; to replace the value use replace() or putOrReplace()");
			}
			return null;
		}

		@Override
		public V get(V find) {
			return first(find);
		}

		@Override
		public int size() {
			return HashMap.this.contains(key) ? 1 : 0;
		}

		@Override
		public V replace(V value) {
			return HashMap.this.replace(key, value);
		}

		@Override
		public V putOrReplace(V value) {
			return HashMap.this.put(key, value);
		}

	}
	
	final class KeySet extends AbstractKeySet implements Set<K> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override
		public K get(K key) {
			return first(key);
		}
		@Override
		public int size() {
			return totalCount();
		}
		@Override
		public Set<K> copy() {
			return HashMap.this.copy().keys();
		}
		@Override
		public Set<K> unique() {
			return this;
		}
	}

	final class EntrySet extends AbstractEntrySet implements Set<Entry<K, V>> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			throw new UnsupportedOperationException();
		}
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> val) {
			throw new UnsupportedOperationException();
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
			return HashMap.this.copy().entries();
		}
		@Override
		public Set<Entry<K, V>> unique() {
			return this;
		}
	}

	@Override
	public Map<K, V> copy() {
		return new HashMap<K, V, N, S>(rehasher, keyEq, nodeEq, nodeFactory, store.copy(nodeProj(), nodeEq));
	}

	// **********************************
	// EQUALITY
	// **********************************
	
	public static abstract class NodeEquality<K, V, N extends HashNode<N> & Entry<K, V>> extends  AbstractHashMap.NodeEquality<K, V, N> {
		private static final long serialVersionUID = -4970889935020537472L;
		public NodeEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq);
		}
		@Override
		public boolean isUnique() {
			return true;
		}
	}	
	
	public static abstract class KeyEquality<K, V, N> extends AbstractHashMap.KeyEquality<K, V, N> {
		private static final long serialVersionUID = -4369240329094683196L;
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq);
		}
		@Override
		public boolean isUnique() {
			return true;
		}
	}

}
