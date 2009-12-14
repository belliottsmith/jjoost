package org.jjoost.collections.maps.base;

import java.util.Iterator;
import java.util.Map.Entry;

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.base.HashStore.HashNode ;
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

	private ScalarSet<Entry<K, V>> entrySet ;
	private MultiSet<K> keySet ;
	
	@Override
	public ScalarSet<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		ScalarSet<Entry<K, V>> r = entrySet ;
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
	public Iterable<V> apply(K v) {
		return values(v) ;
	}

	@Override
	public V put(K key, V val) {
		final N n = nodeFactory.node(hash(key), key, val) ;
		return store.put(n, n, entryEq, valProj()) ;
	}

	@Override
	public V putIfAbsent(K key, V val) {
		final N n = nodeFactory.node(hash(key), key, val) ;
		return store.putIfAbsent(n, n, entryEq, valProj()) ;
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return true ;
	}
	
	@Override
	public MultiMap<K, V> copy() {
		return new InlineMultiHashMap<K, V, N>(keyHasher, rehasher, keyEq, entryEq, nodeFactory, store.copy()) ;
	}

	final class KeySet extends AbstractKeySet implements MultiSet<K> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override public Iterable<K> unique() { 
			return new Iterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.unique(keyProj(), keyEq, keyEq.getKeyEquality(), keyProj()) ;
				}				
			} ;
		}
		@Override
		public boolean permitsDuplicates() {
			return true ;
		}
		@Override
		public MultiSet<K> copy() {
			throw new UnsupportedOperationException() ;
		}
	}

	final class EntrySet extends AbstractEntrySet implements ScalarSet<Entry<K, V>> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override public Iterable<Entry<K, V>> unique() {
			return all() ;
		}
		@Override
		public Entry<K, V> put(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V val = entry.getValue() ;
			final N n = nodeFactory.node(hash(key), key, val) ;
			return store.put(n, n, entryEq, entryProj()) ;
		}
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> entry) {
			final K key = entry.getKey() ;
			final V val = entry.getValue() ;
			final N n = nodeFactory.node(hash(key), key, val) ;
			return store.putIfAbsent(n, n, entryEq, entryProj()) ;
		}
		@Override
		public boolean permitsDuplicates() {
			return false ;
		}
		@Override
		public Entry<K, V> get(Entry<K, V> key) {
			return first(key) ;
		}
		@Override
		public int size() {
			return totalCount() ;
		}
		public ScalarSet<Entry<K, V>> copy() {
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
