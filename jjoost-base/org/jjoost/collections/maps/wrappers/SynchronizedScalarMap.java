package org.jjoost.collections.maps.wrappers;

import java.util.Map.Entry ;

import org.jjoost.collections.ScalarMap;
import org.jjoost.collections.ScalarSet ;
import org.jjoost.util.Factory;
import org.jjoost.util.Function;

public class SynchronizedScalarMap<K, V> extends SynchronizedArbitraryMap<K, V, ScalarMap<K, V>> implements ScalarMap<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedScalarMap(ScalarMap<K, V> delegate) {
		super(delegate) ;
	}
	
	private ScalarSet<K> keySet ;
	private ScalarSet<Entry<K, V>> entrySet ;
	@Override public synchronized ScalarSet<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys()) ;
		return keySet ;
	}
	@Override public synchronized ScalarSet<Entry<K, V>> entries() {
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
	public ScalarMap<K, V> copy() {
		return new SynchronizedScalarMap<K, V>(delegate.copy()) ;
	}

}
