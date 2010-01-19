package org.jjoost.collections.maps.base;

import java.util.Map.Entry ;

import org.jjoost.util.Function ;

public interface OrderedMapNodeFactory<K, V, N> extends Function<Entry<K, V>, N> {

	public N make(K k, V v) ;
	
}
