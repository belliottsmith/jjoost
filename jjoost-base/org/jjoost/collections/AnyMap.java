package org.jjoost.collections;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 
 * @author b.elliottsmith
 *
 * @param <K>
 * @param <V>
 */
public interface AnyMap<K, V> extends AnyReadMap<K, V> {

	public V put(K key, V val) ;
	public V putIfAbsent(K key, V val) ;
	
	/* (non-Javadoc)
	 * @see org.jjoost.collections.ArbitraryReadMap#values(java.lang.Object)
	 */
	@Override public AnySet<V> values(K key) ;
	
	public int remove(K key) ;	
	public int remove(K key, V val) ;
	public Iterable<Entry<K, V>> removeAndReturn(K key) ;	
	public Iterable<Entry<K, V>> removeAndReturn(K key, V val) ;
	public V removeAndReturnFirst(K key) ;	
	
	@Override public AnySet<K> keys() ;
	@Override public AnySet<Entry<K, V>> entries() ;

	public int clear() ;
	public Iterator<Entry<K, V>> clearAndReturn() ;

	public void shrink() ;
	public AnyMap<K, V> copy() ;
	
	public AnyMap<V, K> inverse() ;

	/**
	 * Returns a set representing the range of the map. Operations on this
	 * set will typically be expensive (O(n) where n is the size of the map).
	 * 
	 * @return the range of the map
	 */
	public AnySet<V> values() ;	
	
}
