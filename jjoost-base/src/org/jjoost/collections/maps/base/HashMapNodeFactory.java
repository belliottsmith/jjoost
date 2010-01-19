package org.jjoost.collections.maps.base;

public interface HashMapNodeFactory<K, V, N> {

	public N makeNode(int hash, K key, V value) ;

}
