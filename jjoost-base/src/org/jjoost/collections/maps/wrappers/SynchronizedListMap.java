package org.jjoost.collections.maps.wrappers;

import java.util.Map.Entry ;

import org.jjoost.collections.ListMap;
import org.jjoost.collections.MultiSet ;

public class SynchronizedListMap<K, V> extends SynchronizedArbitraryMap<K, V, ListMap<K, V>> implements ListMap<K, V> {
	
	private static final long serialVersionUID = 2692454383540344975L;
	public SynchronizedListMap(ListMap<K, V> delegate) {
		super(delegate) ;
	}
	
	private MultiSet<K> keySet ;
	private MultiSet<Entry<K, V>> entrySet ;
	@Override public synchronized MultiSet<K> keys() {
		if (keySet == null)
			keySet = wrap(delegate.keys()) ;
		return keySet ;
	}
	@Override public synchronized MultiSet<Entry<K, V>> entries() {
		if (entrySet == null)
			entrySet = wrap(delegate.entries()) ;
		return entrySet ;
	}
	
	@Override public synchronized Iterable<V> apply(K v) {
		return wrap(delegate.apply(v));
	}

	@Override public ListMap<K, V> copy() {
		return new SynchronizedListMap<K, V>(delegate.copy()) ;
	}
	
	@Override public synchronized MultiSet<V> values(K key) {
		return wrap(delegate.values(key)) ;
	}

}