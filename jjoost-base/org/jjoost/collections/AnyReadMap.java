package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;
import java.util.Map.Entry;

public interface AnyReadMap<K, V> extends Serializable {

	public boolean contains(K key) ;	
	public boolean contains(K key, V val) ;
	
	public int count(K key) ;	
	public int count(K key, V val) ;
	
	public V first(K key) ;	
	public List<V> list(K key) ;
	public AnyReadSet<V> values(K key) ;
	
	public AnyReadSet<K> keys() ;
	public Iterable<V> values() ;	
	public AnyReadSet<Entry<K, V>> entries() ;	
	public Iterable<Entry<K, V>> entries(K key) ;
	
	public int totalCount() ;
	public int uniqueKeyCount() ;
	public boolean isEmpty() ;
	public boolean permitsDuplicateKeys() ;

}
