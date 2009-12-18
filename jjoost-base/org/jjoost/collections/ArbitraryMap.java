package org.jjoost.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public interface ArbitraryMap<K, V> extends ArbitraryReadMap<K, V> {

	public V put(K key, V val) ;
	public V putIfAbsent(K key, V val) ;
	
	@Override public ArbitrarySet<V> values(K key) ;
	
	public int remove(K key) ;	
	public int remove(K key, V val) ;
	public Iterable<Entry<K, V>> removeAndReturn(K key) ;	
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) ;
	public V removeAndReturnFirst(K key) ;	
	
	@Override public ArbitrarySet<K> keys() ;
	@Override public ArbitrarySet<Map.Entry<K, V>> entries() ;

	public int clear() ;
	public Iterator<Entry<K, V>> clearAndReturn() ;

	public void shrink() ;
	public ArbitraryMap<K, V> copy() ;
	
	public ArbitraryMap<V, K> inverse() ;

}
