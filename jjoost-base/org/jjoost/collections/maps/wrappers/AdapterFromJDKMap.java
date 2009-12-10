package org.jjoost.collections.maps.wrappers;

import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Iterator ;
import java.util.List ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.jjoost.collections.ArbitraryMap ;
import org.jjoost.collections.ScalarMap ;
import org.jjoost.collections.ScalarSet ;
import org.jjoost.collections.lists.UniformList ;
import org.jjoost.collections.maps.ImmutableMapEntry ;
import org.jjoost.collections.sets.wrappers.AdapterFromJDKSet ;
import org.jjoost.util.Factory ;
import org.jjoost.util.Function ;

public class AdapterFromJDKMap<K, V> implements ScalarMap<K, V> {
	
	private static final long serialVersionUID = -5498331996410891451L ;
	
	private final Map<K, V> map ;
	private ScalarSet<Entry<K, V>> entrySet ;
	private ScalarSet<K> keySet ;
	
	public AdapterFromJDKMap(Map<K, V> map) {
		super() ;
		this.map = map ;
	}
	
	@Override
	public ScalarMap<K, V> copy() {
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
	public ScalarSet<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = new AdapterFromJDKSet<Entry<K, V>>(map.entrySet()) ;
		return entrySet ;
	}
	@Override
	public ArbitraryMap<V, K> inverse() {
		throw new UnsupportedOperationException() ;
	}
	@Override
	public ScalarSet<K> keys() {
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
	public Iterable<V> values() {
		return map.values() ;
	}
	@Override
	public Iterable<V> values(K key) {
		return list(key) ;
	}

}
