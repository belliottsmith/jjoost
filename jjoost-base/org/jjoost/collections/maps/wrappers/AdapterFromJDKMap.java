package org.jjoost.collections.maps.wrappers;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map.Entry ;

import org.jjoost.collections.AnyMap ;
import org.jjoost.collections.Map ;
import org.jjoost.collections.MultiSet;
import org.jjoost.collections.Set ;
import org.jjoost.collections.UnitarySet;
import org.jjoost.collections.iters.EmptyIterator ;
import org.jjoost.collections.iters.UniformIterator ;
import org.jjoost.collections.lists.UniformList ;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.collections.sets.base.IterableSet;
import org.jjoost.collections.sets.wrappers.AdapterFromJDKSet ;
import org.jjoost.util.Equalities;
import org.jjoost.util.Equality;
import org.jjoost.util.Factory ;
import org.jjoost.util.Function ;

// TODO : this class' methods currently assume keys/values stored in it are non null, which is an invalid assumption
public class AdapterFromJDKMap<K, V> implements Map<K, V> {
	
	private static final long serialVersionUID = -5498331996410891451L ;
	
	private final java.util.Map<K, V> map ;
	private Set<Entry<K, V>> entrySet ;
	private Set<K> keySet ;
	
	public AdapterFromJDKMap(java.util.Map<K, V> map) {
		super() ;
		this.map = map ;
	}
	
	@Override
	public Map<K, V> copy() {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		V v = map.get(key) ;
		if (v == null) {
			v = putIfNotPresent.create() ;
			map.put(key, v) ;
		}
		return v ;
	}
	@Override
	public V ensureAndGet(K key, Function<? super K, ? extends V> putIfNotPresent) {
		final V v = map.get(key) ;
		if (v == null)
			map.put(key, putIfNotPresent.apply(key)) ;
		return v ;
	}
	@Override
	public V get(K key) {
		return map.get(key) ;
	}
	@Override
	public V put(K key, V val) {
		return map.put(key, val) ;
	}
	@Override
	public V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		final V v = map.get(key) ;
		if (v == null)
			map.put(key, putIfNotPresent.apply(key)) ;
		return v ;
	}
	@Override
	public V putIfAbsent(K key, V val) {
		final V v = map.get(key) ;
		if (v == null)
			map.put(key, val) ;
		return v ;
	}
	@Override
	public int size() {
		return map.size() ;
	}
	@Override
	public V apply(K v) {
		return map.get(v) ;
	}
	@Override
	public int clear() {
		final int size = map.size() ;
		map.clear() ;
		return size ;
	}
	
	// TODO use a linked list iterator that will reclaim memory as it is consumed
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		List<Entry<K, V>> vals = new ArrayList<Entry<K, V>>() ;
		for (Entry<K, V> entry : map.entrySet())
			vals.add(entry) ;
		map.clear() ;
		return vals.iterator() ;
	}
	@Override
	public Set<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = new AdapterFromJDKSet<Entry<K, V>>(map.entrySet()) ;
		return entrySet ;
	}
	@Override
	public AnyMap<V, K> inverse() {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public Set<K> keys() {
		if (keySet == null)
			keySet = new AdapterFromJDKSet<K>(map.keySet()) ;
		return keySet ;
	}
	@Override
	public int remove(K key, V val) {
		final V e = map.get(key) ;
		if (e != null && e.equals(val)) {
			map.remove(key) ;
			return 1 ;
		}
		return 0 ;
	}
	@Override
	public int remove(K key) {
		if (map.containsKey(key)) {
			map.remove(key) ;
			return 1 ;
		}
		return 0 ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		if (val == null) {
			if (map.containsKey(key)) {
				map.remove(key) ;
				return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, null), 1) ;
			}
		} else {
			final V e = map.get(key) ;
			if (e != null && e.equals(val)) {
				map.remove(key) ;
				return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, e), 1) ;
			}
		}
		return Collections.emptyList() ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		if (map.containsKey(key)) {
			final V v = map.remove(key) ;
			return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, v), 1) ;
		}
		return Collections.emptyList() ;
	}
	@Override
	public V removeAndReturnFirst(K key) {
		return map.remove(key) ;
	}
	@Override
	public void shrink() {
	}
	@Override
	public boolean contains(K key, V val) {
		final V v = map.get(key) ;
		return val == null ? v == null && map.containsKey(key) : v != null && v.equals(val) ; 
	}
	@Override
	public boolean contains(K key) {
		return map.containsKey(key) ;
	}
	@Override
	public int count(K key, V val) {
		return contains(key, val) ? 1 : 0 ;
	}
	@Override
	public int count(K key) {
		return contains(key) ? 1 : 0 ;
	}
	@Override
	public Iterable<Entry<K, V>> entries(K key) {
		if (map.containsKey(key)) {
			return new UniformList<Entry<K, V>>(new ImmutableMapEntry<K, V>(key, map.get(key)), 1) ;
		}
		return Collections.emptyList() ;
	}
	@Override
	public V first(K key) {
		return map.get(key) ;
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty() ;
	}
	@Override
	public List<V> list(K key) {
		if (map.containsKey(key)) {
			return new UniformList<V>(map.get(key), 1) ;
		}
		return Collections.emptyList() ;
	}
	@Override
	public boolean permitsDuplicateKeys() {
		return false ;
	}
	@Override
	public int totalCount() {
		return map.size() ;
	}
	@Override
	public int uniqueKeyCount() {
		return map.size() ;
	}
	@Override
	public MultiSet<V> values() {
		return new IterableSet<V>() {
			private static final long serialVersionUID = 1241705304994308496L;
			@Override
			public Equality<? super V> equality() {
				return Equalities.object();
			}
			@Override
			public Iterator<V> iterator() {
				return map.values().iterator() ;
			}
		} ;
	}
	@Override
	public UnitarySet<V> values(K key) {
		return new KeyValueSet(key) ;
	}
	
	private final class KeyValueSet implements UnitarySet<V> {
		private static final long serialVersionUID = 6651319386421757315L ;
		final K key ;
		public KeyValueSet(K key) {
			this.key = key ;
		}
		@Override
		public int clear() {
			return AdapterFromJDKMap.this.remove(key) ;
		}
		@Override
		public Iterator<V> clearAndReturn() {
			if (map.containsKey(key)) {
				final V v = map.remove(key) ;
				return new UniformIterator<V>(v, 1) ;
			}
			return new EmptyIterator<V>() ;
		}
		@Override
		public int putAll(Iterable<V> val) {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public V putIfAbsent(V val) {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public int remove(V value, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove less than zero items") ;
				return 0 ;
			}
			return remove(value) ;
		}
		@Override
		public int remove(V value) {
			return AdapterFromJDKMap.this.remove(key, value) ;
		}
		@Override
		public Iterable<V> removeAndReturn(V val, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove less than zero items") ;
				return Collections.emptyList() ;
			}
			return removeAndReturn(val) ;
		}
		@Override
		public Iterable<V> removeAndReturn(V val) {
			final V e = map.get(key) ;
			if (e != null && e.equals(val)) {
				map.remove(key) ;
				return new UniformList<V>(val, 1) ;
			}
			return Collections.emptyList() ;
		}
		@Override
		public V removeAndReturnFirst(V val, int removeAtMost) {
			if (removeAtMost < 1) {
				if (removeAtMost < 0)
					throw new IllegalArgumentException("Cannot remove less than zero items") ;
				return null ;
			}
			return removeAndReturnFirst(val) ;
		}
		@Override
		public V removeAndReturnFirst(V val) {
			final V e = map.get(key) ;
			if (e != null && e.equals(val)) {
				map.remove(key) ;
				return val ;
			}
			return null ;
		}
		@Override
		public void shrink() {
		}
		@Override
		public Iterable<V> all(V value) {
			final V val = map.get(key) ;
			if (val == null) {
				return Collections.emptyList() ;
			}
			if (value != null && val.equals(value))
				return new UniformList<V>(val, 1) ;
			return Collections.emptyList() ;
		}
		@Override
		public boolean contains(V val) {
			return AdapterFromJDKMap.this.contains(key, val) ;
		}
		@Override
		public int count(V val) {
			return AdapterFromJDKMap.this.count(key, val) ;
		}
		@Override
		public V first(V value) {
			final V val = map.get(key) ;
			if (val == null) {
				return null ;
			}
			if (value != null && val.equals(value))
				return val ;
			return null ;
		}
		@Override
		public boolean isEmpty() {
			return AdapterFromJDKMap.this.contains(key) ;
		}
		@Override
		public List<V> list(V value) {
			if (map.containsKey(key))
				return new UniformList<V>(value, 1) ;
			return Collections.emptyList() ;
		}
		@Override
		public boolean permitsDuplicates() {
			return false ;
		}
		@Override
		public int totalCount() {
			return AdapterFromJDKMap.this.count(key) ;
		}
		@Override
		public Iterable<V> unique() {
			return this ;
		}
		@Override
		public int uniqueCount() {
			return totalCount() ;
		}
		@Override
		public UnitarySet<V> copy() {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public V get() {
			return map.get(key) ;
		}
		@Override
		public V put(V val) {
			throw new UnsupportedOperationException() ;
		}
		@Override
		public Boolean apply(V v) {
			return contains(v) ;
		}
		@Override
		public Iterator<V> iterator() {
			if (map.containsKey(key)) {
				return new UniformIterator<V>(map.get(key), 1) ;
			}
			return EmptyIterator.get() ;
		}
		@Override
		public Equality<? super V> equality() {
			return Equalities.object() ;
		}
		
	}

}
