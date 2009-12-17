package org.jjoost.collections.maps.base;

import java.util.Map.Entry;

import org.jjoost.collections.ScalarMap;
import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashNodeFactory ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;

public class ScalarHashMap<K, V, N extends HashNode<N> & Entry<K, V>> extends AbstractHashMap<K, V, N> implements ScalarMap<K, V> {

	protected ScalarHashMap(
			Hasher<? super K> keyHasher, Rehasher rehasher, 
			AbstractHashMap.KeyEquality<K, V, N> keyEquality, 
			AbstractHashMap.EntryEquality<K, V, N> entryEquality,
			HashMapNodeFactory<K, V, N> nodeFactory, HashStore<N> table) {
		super(keyHasher, rehasher, keyEquality, entryEquality, nodeFactory, table) ;
	}
	
	private static final long serialVersionUID = -6385620376018172675L;

	private ScalarSet<Entry<K, V>> entrySet ;
	private ScalarSet<K> keySet ;
	
	@Override
	public ScalarSet<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		ScalarSet<Entry<K, V>> r = entrySet ;
		if (r == null)
			entrySet = r = new EntrySet() ;
		return r ;
	}
	@Override
	public ScalarSet<K> keys() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		ScalarSet<K> r = keySet ;
		if (r == null) {
			keySet = r = new KeySet() ;
		}
		return r ;
	}
	
	@Override
	public V put(K key, V val) {
		return store.put(key, nodeFactory.node(hash(key), key, val), keyEq, valProj()) ;
	}

	@Override
	public V putIfAbsent(K key, V val) {
		return store.putIfAbsent(key, nodeFactory.node(hash(key), key, val), keyEq, valProj()) ;
	}

	@Override
	public V ensureAndGet(K key, final Factory<? extends V> putIfNotPresent) {
		return store.ensureAndGet(hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 1L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.node(hash, key, putIfNotPresent.create()) ;
			}
		}, valProj()) ;
	}

	@Override
	public V ensureAndGet(K key, final Function<? super K, ? extends V> putIfNotPresent) {
		return store.ensureAndGet(hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 526770033919300687L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.node(hash, key, putIfNotPresent.apply(key)) ;
			}
		}, valProj());
	}

	@Override
	public V get(K key) {
		return first(key) ;
	}

	@Override
	public V apply(K k) {
		return first(k) ;
	}
	
	@Override
	public V putIfAbsent(final K key, final Function<? super K, ? extends V> putIfNotPresent) {
		return store.putIfAbsent(hash(key), key, keyEq, new HashNodeFactory<K, N>() {
			private static final long serialVersionUID = 3624491527804791117L;
			@Override
			public N makeNode(int hash, K key) {
				return nodeFactory.node(hash, key, putIfNotPresent.apply(key)) ;
			}
		}, valProj()) ;
	}

	@Override
	public boolean permitsDuplicateKeys() {
		return false ;
	}
	
	@Override
	public int size() {
		return totalCount() ;
	}

	final class KeySet extends AbstractKeySet implements ScalarSet<K> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override 
		public Iterable<K> unique() { 
			return all() ; 
		}
		@Override
		public boolean permitsDuplicates() {
			return false ;
		}
		@Override
		public K get(K key) {
			return first(key) ;
		}
		@Override
		public int size() {
			return totalCount() ;
		}
		@Override
		public ScalarSet<K> copy() {
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
			throw new UnsupportedOperationException() ;
		}
		@Override
		public Entry<K, V> putIfAbsent(Entry<K, V> val) {
			throw new UnsupportedOperationException() ;
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

	@Override
	public ScalarMap<K, V> copy() {
		return new ScalarHashMap<K, V, N>(keyHasher, rehasher, keyEq, entryEq, nodeFactory, store.copy()) ;
	}

	// **********************************
	// EQUALITY
	// **********************************
	
	protected static abstract class EntryEquality<K, V, N> extends  AbstractHashMap.EntryEquality<K, V, N> {
		private static final long serialVersionUID = -4970889935020537472L ;
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
			return true ;
		}
	}	

}
