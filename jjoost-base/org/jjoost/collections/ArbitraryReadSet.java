package org.jjoost.collections;

import java.io.Serializable;
import java.util.List;

import org.jjoost.util.Function;

public interface ArbitraryReadSet<V> extends Iterable<V>, Function<V, Boolean>, Serializable {

	public V first(V value) ;
	public Iterable<V> all(V value) ;
	public List<V> list(V value) ;
	
	public boolean contains(V value) ;
	public int count(V value) ;
	
	
	public Iterable<V> all() ;
	public Iterable<V> unique() ;
	
	public int totalCount() ;
	public int uniqueCount() ;	
	public boolean isEmpty() ;
	public boolean permitsDuplicates() ;
	
}
