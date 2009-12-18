package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


public interface ArbitraryReadMap<K, V> extends Serializable {

	public boolean contains(K key) ;	
	public boolean contains(K key, V val) ;
	
	public int count(K key) ;	
	public int count(K key, V val) ;
	
	public V first(K key) ;	
	public List<V> list(K key) ;
	public ArbitraryReadSet<V> values(K key) ;
	
	public ArbitraryReadSet<K> keys() ;
	public Iterable<V> values() ;	
	public ArbitraryReadSet<Map.Entry<K, V>> entries() ;	
	public Iterable<Map.Entry<K, V>> entries(K key) ;
	
	public int totalCount() ;
	public int uniqueKeyCount() ;
	public boolean isEmpty() ;
	public boolean permitsDuplicateKeys() ;

}
