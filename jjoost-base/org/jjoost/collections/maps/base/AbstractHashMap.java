package org.jjoost.collections.maps.base;

import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.collections.ArbitraryMap;
import org.jjoost.collections.ArbitrarySet;
import org.jjoost.collections.base.HashNodeEquality ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Function ;
import org.jjoost.util.Functions;
import org.jjoost.util.Hasher;
import org.jjoost.util.Rehasher;

public abstract class AbstractHashMap<K, V, N extends HashStore.HashNode<N> & Map.Entry<K, V>> implements ArbitraryMap<K, V> {

	protected static abstract class EntryEquality<K, V, N> implements HashNodeEquality<Entry<K, V>, N>, Equality<N> {
		private static final long serialVersionUID = -4970889935020537472L ;
		protected final Equality<? super K> keyEq ;
		protected final Equality<? super V> valEq ;
		public EntryEquality(Equality<? super K> keyEq, Equality<? super V> valEq) { this.keyEq = keyEq ; this.valEq = valEq ; }
		public Equality<? super K> getKeyEquality() { return keyEq ; }
		public Equality<? super V> getValueEquality() { return valEq ; }		
	}
	
	protected static abstract class KeyEquality<K, V, N> implements HashNodeEquality<K, N> {
		protected final Equality<? super K> keyEq ;
		public KeyEquality(Equality<? super K> keyEq) { this.keyEq = keyEq ; }
		@Override 
		public boolean suffixMatch(K key, N n) { return true ; }
		public Equality<? super K> getKeyEquality() { return keyEq ; }		
	}	

	private static final long serialVersionUID = 3187373892419456381L;
	
	protected final HashStore<N> store ;
	protected final Hasher<? super K> keyHasher ;
	protected final Rehasher rehasher ;
	protected final KeyEquality<K, V, N> keyEq ;
	protected final EntryEquality<K, V, N> entryEq ;
	protected final HashMapNodeFactory<K, V, N> nodeFactory ;
	
	protected AbstractHashMap(
			Hasher<? super K> keyHasher, Rehasher rehasher, 
			KeyEquality<K, V, N> keyEquality, 
			EntryEquality<K, V, N> entryEquality, 
			HashMapNodeFactory<K, V, N> nodeFactory, 
			HashStore<N> table) {
		this.store = table ;
		this.keyHasher = keyHasher ;
		this.rehasher = rehasher ;
		this.keyEq = keyEquality ;		
		this.entryEq = entryEquality ;
		this.nodeFactory = nodeFactory ;
	}

	protected final Entry<K, V> entry(K key, V val) {
		return new ImmutableMapEntry<K, V>(key, val) ;
	}
	
	protected final Function<Entry<K, V>, K> keyProj() {
		return Functions.<K, Entry<K, V>>getMapEntryKeyProjection() ;
	}
	
	protected final Function<Entry<K, V>, V> valProj() {
		return Functions.<V, Entry<K, V>>getMapEntryValueProjection() ;
	}
	
	protected final Function<N, N> nodeProj() {
		return Functions.<N>identity() ;
	}
	
	protected final Function<Entry<K, V>, Entry<K, V>> entryProj() {
		return Functions.<Entry<K, V>>identity() ;
	}
	
	protected final int hash(K key) {
		return rehasher.hash(keyHasher.hash(key)) ;
	}
	
	@Override
	public int remove(K key, V val) {
		return store.remove(hash(key), entry(key, val), entryEq) ;
	}
	@Override
	public V removeAndReturnFirst(K key) {
		return store.removeAndReturnFirst(hash(key), key, keyEq, valProj()) ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return store.removeAndReturn(hash(key), entry(key, val), entryEq, entryProj()) ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		return store.removeAndReturn(hash(key), key, keyEq, entryProj()) ;
	}
	@Override
	public int remove(K key) {
		return store.remove(hash(key), key, keyEq) ;
	}
	@Override
	public boolean contains(K key, V val) {
		return store.contains(hash(key), entry(key, val), entryEq) ;
	}
	@Override
	public boolean contains(K key) {
		return store.contains(hash(key), key, keyEq) ;
	}
	@Override
	public int count(K key, V val) {
		return store.count(hash(key), entry(key, val), entryEq) ;
	}
	@Override
	public int count(K key) {
		return store.count(hash(key), key, keyEq) ;
	}
	@Override
	public Iterable<Entry<K, V>> entries(final K key) {
		final int hash = hash(key) ;
		return new Iterable<Entry<K, V>>() {
			@Override
			public Iterator<Entry<K, V>> iterator() {
				return store.find(hash, key, keyEq, keyProj(), entryProj()) ;
			}
		} ;
	}
	@Override
	public void shrink() {
		store.shrink() ;
	}
	@Override
	public V first(K key) {
		return store.first(hash(key), key, keyEq, valProj()) ;
	}
	@Override
	public List<V> list(K key) {
		return store.findNow(hash(key), key, keyEq, valProj()) ;
	}
	@Override
	public int totalCount() {
		return store.totalCount() ;
	}
	@Override
	public int uniqueKeyCount() {
		return store.uniquePrefixCount() ;
	}
	@Override
	public boolean isEmpty() {
		return store.isEmpty() ;
	}
	@Override
	public Iterable<V> values() {
		return new AbstractIterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.all(keyProj(), keyEq, valProj()) ;
			}
		} ;
	}
	@Override
	public Iterable<V> values(final K key) {
		final int hash = hash(key) ;
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return store.find(hash, key, keyEq, keyProj(), valProj()) ;
			}
		} ;
	}

	@Override
	public int clear() {
		return store.clear() ;
	}
	
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		return store.clearAndReturn(entryProj()) ;
	}
	
	@Override
	public ArbitraryMap<V, K> inverse() {
		throw new UnsupportedOperationException() ;
	}
	
	protected abstract class AbstractKeySet extends AbstractIterable<K> implements ArbitrarySet<K> {
		
		private static final long serialVersionUID = 1461826147890179114L ;

		@Override
		public boolean contains(K value) {
			return AbstractHashMap.this.contains(value) ;
		}

		@Override
		public int count(K value) {
			return AbstractHashMap.this.count(value) ;			
		}

		@Override
		public void shrink() {
			AbstractHashMap.this.shrink() ;			
		}
		
		@Override
		public int totalCount() {
			return AbstractHashMap.this.totalCount() ;			
		}

		@Override
		public int clear() {
			return AbstractHashMap.this.clear() ;			
		}
		
		@Override
		public Iterator<K> clearAndReturn() {
			return store.clearAndReturn(Functions.<K, Entry<K, V>>getMapEntryKeyProjection()) ;			
		}

		@Override
		public int remove(K key) {
			return AbstractHashMap.this.remove(key) ;			
		}

		@Override
		public Boolean apply(K v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
		}

		@Override
		public Iterable<K> all() {
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.all(keyProj(), keyEq, keyProj()) ;
				}
			} ;
		}
		
		@Override
		public Iterable<K> all(final K key) {
			final int hash = hash(key) ;
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.find(hash, key, keyEq, keyProj(), keyProj()) ;
				}
			} ;
		}
		
		@Override
		public K first(final K key) {
			return store.first(hash(key), key, keyEq, keyProj()) ;
		}
		
		@Override
		public List<K> list(final K key) {
			return store.findNow(hash(key), key, keyEq, keyProj()) ;
		}
		
		@Override
		public Iterator<K> iterator() {
			return store.all(keyProj(), keyEq, keyProj()) ;
		}

		@Override
		public boolean isEmpty() {
			return AbstractHashMap.this.isEmpty() ;
		}

		@Override
		public int uniqueCount() {
			return AbstractHashMap.this.uniqueKeyCount() ;
		}

		@Override
		public final K put(K val) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public final K putIfAbsent(K val) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public Iterable<K> removeAndReturn(K key) {
			return store.removeAndReturn(hash(key), key, keyEq, keyProj());
		}
		
		@Override
		public K removeAndReturnFirst(K key) {
			return store.removeAndReturnFirst(hash(key), key, keyEq, keyProj()) ;
		}
		
		@Override
		public int putAll(Iterable<K> vals) {
			throw new UnsupportedOperationException() ;
		}

	}
	
	protected abstract class AbstractEntrySet extends AbstractIterable<Entry<K, V>> implements ArbitrarySet<Entry<K, V>> {
		
		private static final long serialVersionUID = 4037233101289518536L ;

		@Override
		public boolean contains(Entry<K, V> value) {
			return AbstractHashMap.this.contains(value.getKey(), value.getValue()) ;
		}

		@Override
		public int count(Entry<K, V> value) {
			return AbstractHashMap.this.count(value.getKey(), value.getValue()) ;
		}

		@Override
		public int totalCount() {
			return AbstractHashMap.this.totalCount() ;			
		}

		@Override
		public int clear() {
			return AbstractHashMap.this.clear() ;			
		}

		@Override
		public Iterator<Entry<K, V>> clearAndReturn() {
			return AbstractHashMap.this.clearAndReturn() ;			
		}

		@Override
		public void shrink() {
			AbstractHashMap.this.shrink() ;			
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry) {
			return AbstractHashMap.this.remove(entry.getKey(), entry.getValue()) ;			
		}

		@Override
		public Boolean apply(Entry<K, V> v) {
			return contains(v) ? Boolean.TRUE : Boolean.FALSE ;
		}
		
		@Override
		public Iterable<Entry<K, V>> all() {
			return new AbstractIterable<Entry<K,V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.all(keyProj(), keyEq, entryProj()) ;
				}
			} ;
		}

		@Override
		public Iterable<Entry<K, V>> all(final Entry<K, V> entry) {
			final int hash = hash(entry.getKey()) ;
			return new AbstractIterable<Entry<K,V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.find(hash, entry, entryEq, nodeProj(), entryProj()) ;
				}
			} ;
		}

		@Override
		public Entry<K, V> first(Entry<K, V> entry) {
			return store.first(hash(entry.getKey()), entry, entryEq, nodeProj()) ;			
		}

		@Override
		public List<Entry<K, V>> list(Entry<K, V> entry) {
			return store.findNow(hash(entry.getKey()), entry, entryEq, entryProj()) ;
		}

		@Override
		public Iterator<Entry<K, V>> iterator() {
			return store.all(keyProj(), keyEq, entryProj()) ;
		}
		
		@Override
		public boolean isEmpty() {
			return AbstractHashMap.this.isEmpty() ;
		}
		
		@Override
		public int uniqueCount() {
			return AbstractHashMap.this.totalCount() ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry) {
			return AbstractHashMap.this.removeAndReturn(entry.getKey(), entry.getValue()) ;
		}
		
		@Override
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0 ;
			for (final Entry<K, V> val : vals)
				if (put(val) == null)
					c++ ;
			return c ;
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry) {
			final Iterator<Entry<K, V>> iter = removeAndReturn(entry).iterator() ;
			return iter.hasNext() ? iter.next() : null ;
		}
		
	}

}
