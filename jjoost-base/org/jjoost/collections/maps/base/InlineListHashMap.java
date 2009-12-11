package org.jjoost.collections.maps.base;

import java.util.Iterator;
import java.util.Map.Entry;

import org.jjoost.collections.ListMap;
import org.jjoost.collections.ListSet ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.util.Equality;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;

public class InlineListHashMap<K, V, N extends Entry<K, V>> extends AbstractHashMap<K, V, N> implements ListMap<K, V> {

	protected InlineListHashMap(
			Hasher<? super K> keyHasher, Rehasher rehasher, 
			AbstractHashMap.KeyEquality<K, V, N> keyEquality, 
			AbstractHashMap.EntryEquality<K, V, N> entryEquality,
			HashMapNodeFactory<K, V, N> nodeFactory, HashStore<N> table) {
		super(keyHasher, rehasher, keyEquality, entryEquality, nodeFactory, table) ;
	}
	
	private static final long serialVersionUID = -6385620376018172675L;

	private ListSet<Entry<K, V>> entrySet ;
	private ListSet<K> keySet ;
	
	@Override
	public ListSet<Entry<K, V>> entries() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		ListSet<Entry<K, V>> r = entrySet ;
		if (r == null)
			entrySet = r = new EntrySet() ;
		return r ;
	}
	@Override
	public ListSet<K> keys() {
		// don't care if we create multiple of these with multiple threads - eventually all but one of them will disappear and don't want to synchronize on every call
		ListSet<K> r = keySet ;
		if (r == null) {
			keySet = r = new KeySet() ;
		}
		return r ;
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
	public Iterable<V> apply(K v) {
		return values(v) ;
	}
	
	@Override
	public boolean permitsDuplicateKeys() {
		return true ;
	}

	@Override
	public ListMap<K, V> copy() {
		return new InlineListHashMap<K, V, N>(keyHasher, rehasher, keyEq, entryEq, nodeFactory, store.copy()) ;
	}
	
	final class KeySet extends AbstractKeySet implements ListSet<K> {
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
		public ListSet<K> copy() {
			throw new UnsupportedOperationException() ;
		}
	}

	final class EntrySet extends AbstractEntrySet implements ListSet<Entry<K, V>> {
		private static final long serialVersionUID = 2741936401896784235L;
		@Override public Iterable<Entry<K, V>> unique() {
			return new Iterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.unique(nodeProj(), entryEq, entryEq, entryProj()) ;
				}
			};
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
			return true ;
		}
		@Override
		public ListSet<Entry<K, V>> copy() {
			throw new UnsupportedOperationException() ;
		}
	}

	// *****************************************
	// EQUALITY
	// *****************************************
	
//	public static <K, V> InsertionEquality<K, V> equality(Equality<? super K> keyEq, Equality<? super V> valEq) {
//		return new InsertionEquality<K, V>(keyEq) ;
//	}
//	protected static final class InsertionEquality<K, V> implements HashNodeEquality<Entry<K, V>, Entry<K, V>> {
//		final Equality<? super K> keyEq ;
//		public InsertionEquality(Equality<? super K> keyEq) { this.keyEq = keyEq ; }
//		@Override 
//		public boolean suffixMatch(Entry<K, V> cmp, Entry<K, V> n) { 
//			return false ;  
//		}
//		@Override 
//		public boolean prefixMatch(Entry<K, V> n1, Entry<K, V> n2) { 
//			return keyEq.equates(n1.getKey(), n2.getKey()) ; 
//		}
//		@Override
//		public boolean isUnique() {
//			return false ;
//		}		
//	}	
//	
	protected static abstract class EntryEquality<K, V, N> extends AbstractHashMap.EntryEquality<K, V, N> {		
		private static final long serialVersionUID = -925214185778609894L ;
		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) {
			super(keyEq, valEq) ;
		}
		@Override
		public boolean isUnique() {
			return false ;
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
