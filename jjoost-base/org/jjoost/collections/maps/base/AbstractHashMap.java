package org.jjoost.collections.maps.base;

import java.util.Iterator;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jjoost.collections.ArbitraryMap;
import org.jjoost.collections.ArbitrarySet;
import org.jjoost.collections.base.HashNode ;
import org.jjoost.collections.base.HashNodeEquality ;
import org.jjoost.collections.base.HashStore ;
import org.jjoost.collections.iters.AbstractIterable ;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.util.Equality ;
import org.jjoost.util.Filters ;
import org.jjoost.util.Function ;
import org.jjoost.util.Functions;
import org.jjoost.util.Hasher;
import org.jjoost.util.Iters ;
import org.jjoost.util.Rehasher;

public abstract class AbstractHashMap<K, V, N extends HashNode<N> & Map.Entry<K, V>> implements ArbitraryMap<K, V> {

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
		return store.remove(hash(key), Integer.MAX_VALUE, entry(key, val), entryEq) ;
	}
	@Override
	public V removeAndReturnFirst(K key) {
		return store.removeAndReturnFirst(hash(key), Integer.MAX_VALUE, key, keyEq, valProj()) ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		return store.removeAndReturn(hash(key), Integer.MAX_VALUE, entry(key, val), entryEq, entryProj()) ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		return store.removeAndReturn(hash(key), Integer.MAX_VALUE, key, keyEq, entryProj()) ;
	}
	@Override
	public int remove(K key) {
		return store.remove(hash(key), Integer.MAX_VALUE, key, keyEq) ;
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
				return store.find(hash, key, keyEq, nodeProj(), entryEq, entryProj()) ;
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
	
	protected abstract class AbstractKeyValueSet extends AbstractIterable<V> implements ArbitrarySet<V> {
		
		private static final long serialVersionUID = 1461826147890179114L ;
		
		private final int hash ;
		private final K key ;
		
		public AbstractKeyValueSet(K key) {
			this.key = key ;
			this.hash = hash(key) ;
		}
		
		@Override
		public boolean contains(V value) {
			return AbstractHashMap.this.contains(key, value) ;
		}
		
		@Override
		public int count(V value) {
			return AbstractHashMap.this.count(key, value) ;			
		}
		
		@Override
		public void shrink() {						
		}
		
		@Override
		public int totalCount() {
			return store.count(hash, key, keyEq) ;
		}
		
		@Override
		public int clear() {
			return store.remove(hash, Integer.MAX_VALUE, key, keyEq) ;
		}
		
		@Override
		public Iterator<V> clearAndReturn() {
			return store.removeAndReturn(hash, Integer.MAX_VALUE, key, keyEq, valProj()).iterator() ;			
		}
		
		@Override
		public Boolean apply(V v) {
			return store.contains(hash, entry(key, v), entryEq) ;
		}
		
		@Override
		public Iterable<V> all() {
			return this ;
		}
		
		@Override
		public Iterable<V> all(final V v) {
			return new AbstractIterable<V>() {
				@Override
				public Iterator<V> iterator() {
					return store.find(hash, entry(key, v), entryEq, entryProj(), entryEq, valProj()) ;
				}
			} ;
		}
		
		@Override
		public V first(final V val) {
			return store.first(hash, entry(key, val), entryEq, valProj()) ;
		}
		
		@Override
		public List<V> list(final V val) {
			return store.findNow(hash, entry(key, val), entryEq, valProj()) ;
		}
		
		@Override
		public Iterator<V> iterator() {
			return store.find(hash, key, keyEq, entryProj(), entryEq, valProj()) ;
		}
		
		@Override
		public boolean isEmpty() {
			return store.contains(hash, key, keyEq) ;
		}
		
		@Override
		public int uniqueCount() {
			if (entryEq.isUnique())
				return totalCount() ;
			return Iters.count(Filters.apply(Filters.unique(entryEq.valEq), iterator())) ;
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !entryEq.isUnique() ;
		}
		
		@Override
		public Iterable<V> unique() {
			if (entryEq.isUnique())
				return this ;
			return Filters.apply(Filters.unique(entryEq.valEq), this) ;
		}
		
		@Override
		public final V put(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException() ;
			final N insert = nodeFactory.node(hash, key, val) ;
			return store.put(insert, insert, entryEq, valProj()) ;
		}
		
		@Override
		public final V putIfAbsent(V val) {
			if (keyEq.isUnique())
				throw new UnsupportedOperationException() ;
			final N insert = nodeFactory.node(hash, key, val) ;
			return store.putIfAbsent(insert, insert, entryEq, valProj()) ;
		}
		
		@Override
		public Iterable<V> removeAndReturn(V val) {
			return store.removeAndReturn(hash, Integer.MAX_VALUE, entry(key, val), entryEq, valProj());
		}
		
		@Override
		public int remove(V val) {
			return store.remove(hash, Integer.MAX_VALUE, entry(key, val), entryEq);
		}
		
		@Override
		public V removeAndReturnFirst(V val) {
			return store.removeAndReturnFirst(hash, Integer.MAX_VALUE, entry(key, val), entryEq, valProj());
		}
		
		@Override
		public int putAll(Iterable<V> vals) {
			throw new UnsupportedOperationException() ;
		}
		
		@Override
		public int remove(V val, int atMost) {
			return store.remove(hash, atMost, entry(key, val), entryEq);
		}
		
		@Override
		public V removeAndReturnFirst(V val, int atMost) {
			return store.removeAndReturnFirst(hash, atMost, entry(key, val), entryEq, valProj()) ;
		}
		
		@Override
		public Iterable<V> removeAndReturn(V val, int atMost) {
			return store.removeAndReturn(hash, atMost, entry(key, val), entryEq, valProj());
		}
		
	}
	
	protected abstract class AbstractKeySet extends AbstractIterable<K> implements ArbitrarySet<K> {
		
		private static final long serialVersionUID = 1461826147890179114L ;

		@Override 
		public Iterable<K> unique() {
			if (keyEq.isUnique())
				return this ;
			return new AbstractIterable<K>() {
				@Override
				public Iterator<K> iterator() {
					return store.unique(keyProj(), keyEq.getKeyEquality(), nodeProj(), entryEq, keyProj()) ;
				}
			} ;
		}
		@Override
		public boolean permitsDuplicates() {
			return !keyEq.isUnique() ;
		}
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
					return store.find(hash, key, keyEq, nodeProj(), entryEq, keyProj()) ;
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
			return store.removeAndReturn(hash(key), Integer.MAX_VALUE, key, keyEq, keyProj());
		}
		
		@Override
		public int remove(K key) {
			return store.remove(hash(key), Integer.MAX_VALUE, key, keyEq) ;
		}

		@Override
		public K removeAndReturnFirst(K key) {
			return store.removeAndReturnFirst(hash(key), Integer.MAX_VALUE, key, keyEq, keyProj()) ;
		}
		
		@Override
		public int putAll(Iterable<K> vals) {
			throw new UnsupportedOperationException() ;
		}

		@Override
		public int remove(K key, int atMost) {
			return store.remove(hash(key), atMost, key, keyEq) ;
		}
		
		@Override
		public K removeAndReturnFirst(K key, int atMost) {
			return store.removeAndReturnFirst(hash(key), atMost, key, keyEq, keyProj()) ;
		}
		
		@Override
		public Iterable<K> removeAndReturn(K key, int atMost) {
			return store.removeAndReturn(hash(key), atMost, key, keyEq, keyProj());
		}
		
	}
	
	protected abstract class AbstractEntrySet extends AbstractIterable<Entry<K, V>> implements ArbitrarySet<Entry<K, V>> {
		
		private static final long serialVersionUID = 4037233101289518536L ;

		@Override 
		public Iterable<Entry<K, V>> unique() {
			if (entryEq.isUnique())
				return this ;
			return new Iterable<Entry<K, V>>() {
				@Override
				public Iterator<Entry<K, V>> iterator() {
					return store.unique(nodeProj(), entryEq, nodeProj(), entryEq, entryProj()) ;
				}
			};
		}
		
		@Override
		public boolean permitsDuplicates() {
			return !entryEq.isUnique() ;
		}

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
					return store.find(hash, entry, entryEq, nodeProj(), entryEq, entryProj()) ;
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
		public int putAll(Iterable<Entry<K, V>> vals) {
			int c = 0 ;
			for (final Entry<K, V> val : vals)
				if (put(val) == null)
					c++ ;
			return c ;
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry, int atMost) {
			return store.removeAndReturnFirst(hash(entry.getKey()), atMost, entry, entryEq, entryProj()) ;
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry, int atMost) {
			return store.remove(hash(entry.getKey()), atMost, entry, entryEq) ;
		}

		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry, int atMost) {
			return store.removeAndReturn(hash(entry.getKey()), atMost, entry, entryEq, entryProj()) ;
		}
		
		@Override
		public Entry<K, V> removeAndReturnFirst(Entry<K, V> entry) {
			return store.removeAndReturnFirst(hash(entry.getKey()), Integer.MAX_VALUE, entry, entryEq, entryProj()) ;
		}
		
		@Override
		public int remove(Map.Entry<K, V> entry) {
			return store.remove(hash(entry.getKey()), Integer.MAX_VALUE, entry, entryEq) ;
		}
		
		@Override
		public Iterable<Entry<K, V>> removeAndReturn(Entry<K, V> entry) {
			return store.removeAndReturn(hash(entry.getKey()), Integer.MAX_VALUE, entry, entryEq, entryProj()) ;
		}
		
	}

}
