package org.jjoost.collections.maps.base;

import java.util.Map.Entry;

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.Set ;
import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;

public class InlineMultiHashMap<K, V, N extends HashNode<N> & Entry<K, V>> extends AbstractHashMap<K, V, N> implements MultiMap<K, V> {

	protected InlineMultiHashMap(
			Hasher<? super K> keyHasher, Rehasher rehasher, 
			AbstractHashMap.KeyEquality<K, V, N> keyEquality, 
			AbstractHashMap.EntryEquality<K, V, N> entryEquality,
			HashMapNodeFactory<K, V, N> nodeFactory, HashStore<N> table) {
		super(keyHasher, rehasher, keyEquality, entryEquality, nodeFactory, table) ;
	}
	
	private static final long serialVersionUID = -6385620376018172675L;

	private Set<Entry<K, V>> entrySet ;
	private MultiSet<K> keySet ;
	
	@Override
	public Set<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		Set<Entry<K, V>> r = entrySet ;
		if (r == null)
			entrySet = r = new EntrySet() ;
		return r ;
	}
	@Override
	public MultiSet<K> keys() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		MultiSet<K> r = keySet ;
		if (r == null) {
			keySet = r = new KeySet() ;
		}
		return r ;
	}
	
	@Override
	public int uniqueKeyCount() {
		return store.uniquePrefixCount() ;
	}
	
	@Override
	public Set<V> values(K k) {
		return new KeyValueSet(k) ;
	}
	
	@Override
	public Iterable<V> apply(K v) {
		return values(v) ;
	}

	@Override
	public V put(K key, V val) {
		final N n = nodeFactory.makeNode(hash(key), key, val) ;
		return store.put(n, n, entryEq, valProj()) ;
	}

	@Override
	public V putIfAbsent(K key, V val) {
		final N n = nodeFactory.makeNode(hash(key), key, val) ;
		return store.putIfAbsent(n, n, entryEq, valProj()) ;
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return true ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		return new InlineMultiHashMap<K, V, N>(keyHasher, rehasher, keyEq, entryEq, nodeFactory, store.copy(nodeProj(), entryEq)) ;
	}

	final class KeyValueSet extends AbstractKeyValueSet implements Set<V> {
		private static final long serialVersionUID = 2741936401896784235L;
		public KeyValueSet(K key) {
			super(key) ;
		}
		@Override
		public Set<V> copy() {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public V get(V key) {
			return first(key) ;
		}
		@Override
		public int size() {
			return totalCount() ;
		}
	}
	
	final class KeySet extends AbstractKeySet implements MultiSet<K> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override
		public MultiSet<K> copy() {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public void put(K val, int numberOfTimes) {
			throw new UnsupportedOperationException() ;
		}
	}

	final class EntrySet extends AbstractEntrySet implements Set<Entry<K, V>> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V val = entry.getValue() ;
			final N n = nodeFactory.makeNode(hash(key), key, val) ;
			return store.put(n, n, entryEq, entryProj()) ;
		}
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V val = entry.getValue() ;
			final N n = nodeFactory.makeNode(hash(key), key, val) ;
			return store.putIfAbsent(n, n, entryEq, entryProj()) ;
		}
		@Override
		public Entry<K, V> get(Entry<K, V> key) {
			return first(key) ;
		}
		@Override
		public int size() {
			return totalCount() ;
		}
		public Set<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}
	}

	// *****************************************
	// EQUALITY
	// *****************************************
	

	protected static abstract class EntryEquality<K, V, N> extends AbstractHashMap.EntryEquality<K, V, N> {
		private static final long serialVersionUID = -5082864991691726065L ;
		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean isUnique() {
			return true ;
		}
	}	
	
	protected static abstract class KeyEquality<K, V, N> extends AbstractHashMap.KeyEquality<K, V, N> {
		public KeyEquality(Equality<? super K> keyEq) {
			super(keyEq) ;
		}
		@Override
		public boolean isUnique() {
			return false ;
		}
	}	

}
