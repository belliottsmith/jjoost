package org.jjoost.collections.bimaps;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jjoost.collections.AnyMap;
import org.jjoost.collections.ExtendedIterable;
import org.jjoost.collections.MultiSet;

// TODO : all methods returning iterators should be wrapped so that calls to the remove() methods can be trapped to prevent inconsistent state
public abstract class AbstractBiMap<
	K, V, 
	M extends AnyMap<K, V>, 
	I extends AnyMap<V, K>> 
implements AnyMap<K, V> {
	
	private static final long serialVersionUID = 2790620014005060840L ;
	
	protected final M map ;
	protected abstract AbstractBiMap<V, K, I, M> partner() ;
	
	protected AbstractBiMap(M forwards) {
		super();
		this.map = forwards ;
	}
	
	@Override
	public int clear() {
		partner().map.clear() ;
		return map.clear() ;
	}
	@Override
	public Iterator<Entry<K, V>> clearAndReturn() {
		// TODO implement
		throw new UnsupportedOperationException() ;
	}
	@Override
	public AnyMap<V, K> inverse() {
		return partner() ;
	}
	@Override
	public V put(K key, V val) {
		final V v = map.put(key, val) ;
		if (v != null)
			partner().map.remove(v, key) ;
		final K k = partner().map.put(val, key) ;
		if (k != null)
			map.remove(k, val) ;
		return v ;
	}
	@Override
	public V putIfAbsent(K key, V val) {
		final V v = map.putIfAbsent(key, val) ;
		if (v != null)
			return v ;
		final K k = partner().map.put(val, key) ;
		if (k != null) {
			map.remove(key, val) ;
			return val ;
		}
		return null ;
	}
	@Override
	public int remove(K key, V val) {
		partner().map.remove(val, key) ;
		return map.remove(key, val) ;
	}
	@Override
	public int remove(K key) {
		int c = 0 ;
		for (Entry<K, V> entry : map.removeAndReturn(key)) {
			c++ ;
			partner().map.remove(entry.getValue(), entry.getKey()) ;
		}
		return c ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) {
		partner().map.remove(val, key) ;
		return map.removeAndReturn(key, val) ;
	}
	@Override
	public Iterable<Entry<K, V>> removeAndReturn(K key) {
		final Iterable<Entry<K, V>> removed = map.removeAndReturn(key) ;
		for (Entry<K, V> entry : removed) {
			partner().map.remove(entry.getValue(), entry.getKey()) ;
		}
		return removed ;
	}
	@Override
	public V removeAndReturnFirst(K key) {
		V first = null ;
		final Iterable<Entry<K, V>> removed = map.removeAndReturn(key) ;
		for (Entry<K, V> entry : removed) {
			first = entry.getValue() ;
			partner().map.remove(entry.getValue(), entry.getKey()) ;
		}
		return first ;
	}
	@Override
	public void shrink() {
		partner().map.shrink() ;
		map.shrink() ;
	}
	@Override
	public boolean contains(K key, V val) {
		return map.contains(key, val) ;
	}
	@Override
	public boolean contains(K key) {
		return map.contains(key) ;
	}
	@Override
	public int count(K key, V val) {
		return map.count(key, val) ;
	}
	@Override
	public int count(K key) {
		return map.count(key) ;
	}
	@Override
	public Iterable<Entry<K, V>> entries(K key) {
		return map.entries(key) ;
	}
	@Override
	public V first(K key) {
		return map.first(key) ;
	}
	@Override
	public boolean isEmpty() {
		return map.isEmpty() ;
	}
	@Override
	public List<V> list(K key) {
		return map.list(key) ;
	}
	@Override
	public boolean permitsDuplicateKeys() {
		return map.permitsDuplicateKeys() ;
	}
	@Override
	public int totalCount() {
		return map.totalCount() ;
	}
	@Override
	public int uniqueKeyCount() {
		return map.uniqueKeyCount() ;
	}
	@Override
	public MultiSet<V> values() {
		return map.values() ;
	}
	
}
