package org.jjoost.collections.maps.wrappers;

import java.util.Map.Entry ;

import org.jjoost.collections.MultiSet ;
import org.jjoost.collections.MultiMap;
import org.jjoost.collections.ScalarSet ;

public class SynchronizedMultiMap<K, V> extends SynchronizedArbitraryMap<K, V, MultiMap<K, V>> implements MultiMap<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedMultiMap(MultiMap<K, V> delegate) {
		super(delegate) ;
	}
	
	private MultiSet<K> keySet ;
	private ScalarSet<Entry<K, V>> entrySet ;
	@Override public synchronized MultiSet<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys()) ;
		return keySet ;
	}
	@Override public synchronized ScalarSet<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = wrap(delegate.entries()) ;
		return entrySet ;
	}
	
	@Override public synchronized Iterable<V> apply(K v) {
		return wrap(delegate.apply(v)) ;
	}
	@Override public synchronized MultiMap<K, V> copy() {
		return new SynchronizedMultiMap<K, V>(delegate.copy()) ;
	}
	@Override public synchronized ScalarSet<V> values(K key) {
		return wrap(delegate.values(key)) ;
	}

}
