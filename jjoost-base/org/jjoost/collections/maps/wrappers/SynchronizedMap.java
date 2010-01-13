package org.jjoost.collections.maps.wrappers;

import java.util.Map.Entry ;

import org.jjoost.collections.Map;
import org.jjoost.collections.Set ;
import org.jjoost.collections.UnitarySet;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class SynchronizedMap<K, V> extends SynchronizedArbitraryMap<K, V, Map<K, V>> implements Map<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedMap(Map<K, V> delegate) {
		super(delegate) ;
	}
	
	private Set<K> keySet ;
	private Set<Entry<K, V>> entrySet ;
	@Override public synchronized Set<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys()) ;
		return keySet ;
	}
	@Override public synchronized Set<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = wrap(delegate.entries()) ;
		return entrySet ;
	}
	
	@Override public synchronized V apply(K v) {
		return delegate.apply(v);
	}
	@Override public synchronized V ensureAndGet(K key, Factory<? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}
	@Override public synchronized V ensureAndGet(K key,
			Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.ensureAndGet(key, putIfNotPresent);
	}
	@Override public synchronized V get(K key) {
		return delegate.get(key);
	}
	@Override public synchronized int size() {
		return delegate.size();
	}

	@Override public synchronized V putIfAbsent(K key, Function<? super K, ? extends V> putIfNotPresent) {
		return delegate.putIfAbsent(key, putIfNotPresent) ;
	}

	@Override
	public Map<K, V> copy() {
		return new SynchronizedMap<K, V>(delegate.copy()) ;
	}
	@Override public synchronized UnitarySet<V> values(K key) {
		return wrap(delegate.values(key)) ;
	}

}
